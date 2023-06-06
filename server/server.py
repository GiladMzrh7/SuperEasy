from TcpEncryptedSocket import EncSocket
import threading
from protocol import *
import random
import string
from SQL_ORM import DB_Handler
from SQL_ORM import Shop
import json
import googlemaps
import math

threads = []
lock = threading.Lock()
ADDR = ("0.0.0.0", 27391)
server = EncSocket(ADDR, False)
USER_SOCKS = {}
handler = DB_Handler()

MAX_DIST = 45000

TOKEN = "pk.5f18c4bdfe958fed4f2b181f73b754c8"
FROM_CORDS_TO_ADD = "/v1/reverse.php"

API_KEY = "AIzaSyDL2r9jQeu6oENdZAosopyt7RGpDTktQfA"
google = googlemaps.Client(API_KEY)

CLIENT_DATA = {}

def gen_salt():
    return "".join([random.choice(string.printable) for i in range(6)])


def register(uname, pwd):
    return handler.register(uname, pwd, gen_salt())


def login(uname, pwd):
    if handler.login(uname, pwd):
        return "SUC", "WELCOME"
    else:
        return "ERR", "incorrect password or username"


def get_address_from_coordinates(lon, lat):
    """getting address from cords using googlemaps API

    Args:
        lon: longtitude
        lat: latitude

    Returns:
        str: addresses
    """
    
    result = google.reverse_geocode((lat,lon), language="he")[0]['formatted_address'] 
    return "ADR",result


def handle_distances(sock:socket.socket, client_address:str, shops:list[Shop]):
    
    addresses = [shop.address for shop in shops]

    print("checking" , len(addresses), "shops")
    done = 0
    while done < len(shops) - 1:
        distance_data = google.distance_matrix(client_address, addresses[done:done +25])['rows'][0]

        for dic in distance_data['elements']:
            distance = dic

            if distance['status'] != 'OK':
                continue

            result = distance['distance']
            CLIENT_DATA[sock.getpeername()]['addresses'].append(int(result['value']))
            done += 1
    
    CLIENT_DATA[sock.getpeername()]['done'] = True

def get_shop_list(sock: socket.socket,client_address:str, near:float, cheap:float, good:float, products:list) -> list:
    """
    This will take the user input and return suggested shops

    Args:
        client_address (str): the address of the client
        near (float): 0-100% of how near the client wants his shop to be
        cheap (float): 0-100% how cheap the shop should be
        good (float): 0-100% based on other's opinion how good is the shop
        products (list): the products the client wants to buy

    Returns:
        list: list of all shops that passed the threshold
    """
    near = max(near, 0.001)
    cheap = max(cheap, 0.001)
    good = max(good, 0.001)
    
    
    #dictionary used to communicate between threads
    CLIENT_DATA[sock.getpeername()] = {"removed":[], "done":False, "addresses":[]}
    
    #getting shop data
    shops: list[Shop] = handler.get_all_shops_data()
    
    #starting the thread
    t = threading.Thread(target=handle_distances, args=(sock,client_address,shops))
    t.start()
    
    allowed: list[Shop] = []
    max_allowed = near * MAX_DIST

    for i,shop in enumerate(shops):
        shop.recom = 0
        shop.price = handler.get_price_by_shop(shop,products)

        if not shop.price:
            CLIENT_DATA[sock.getpeername()]['removed'].append(i) # if the shop doesn't contain all of the products we remove it
            continue
        
        #the rating to be check with
        RATING_SAF = 5 * good/ 100
        shop.rating = handler.get_rating_by_shop(shop)
        if  shop.rating >= RATING_SAF: # if bigger than rating it's full points
            shop.recom += 33
            
        else:
            shop.recom += 33 * shop.rating/RATING_SAF
        
        allowed.append(shop)


    cheap_index = math.floor(cheap/100 * len(allowed)) # getting the index of realtivly cheap
    
    prices = sorted(allowed, key = lambda x: x.price) # sorting the list

    relative_price = prices[cheap_index].price # this will be the relative price 

    for i,v in enumerate(prices):
        if i in  CLIENT_DATA[sock.getpeername()]['removed']: continue # if the shops doesnt contain all of the products we remove it
        shop.recom += 33 * (relative_price/v.price)
    
    for i,s in enumerate(shops):
        while not CLIENT_DATA[sock.getpeername()]['done']: # waiting for the thread to finish calculating the distance
            continue
        
        if i in  CLIENT_DATA[sock.getpeername()]['removed']: 
            continue
        
        distance = CLIENT_DATA[sock.getpeername()]['addresses'][i] #getting the distance
        s.distance = distance

        if distance < max_allowed:
            s.recom += 33
        
        elif distance >= MAX_DIST:
            continue
        
        else:
            pct = distance/max_allowed
            s.recom += 33 * pct
        
    sort:list[Shop] =  sorted(allowed,key= lambda x: x.recom, reverse=True) # sorting the array by recomendation rating
    
    t.join() # closing the thread
    
    return {s.name + "|" + s.address:[f'{s.price}', f'{s.recom}', f'{s.distance}',f'{s.rating}',f'{s.id}'] for s in sort} # returning data

def handle_get(sock:socket.socket):
    names = handler.GetAllProductsName()[:50]
    codes = handler.GetAllProductsCode()[:50]
    merged_dic = {name:code for name,code in zip(names,codes)}
    json_string = json.dumps(merged_dic, ensure_ascii=False).encode()
    return send_by_chunks(json_string,1,server,sock)

def handle_rating(rating, id):
    print(id)
    num,summ = handler.get_rating_data_by_id(id)
    new_sum = int(summ) + int(rating)
    new_num = num+1
    new_rating = new_sum/new_num
    handler.set_rating_data_by_id(id, new_rating, new_sum, new_num)
    


def handle_loc(data):
    

    latitude = data[0]
    longitude = data[1]
    
    to_ret = get_address_from_coordinates(longitude, latitude)
    ADDRESS = to_ret[1]
    
    return ADDRESS,to_ret


def handle_client(sock: socket.socket):
    ADDRESS = None
    PRODUCTS = None
    while True:
        to_ret = "ERR", "Internal"
        
        cmd, *data = server.recieve(sock)
        print(cmd, data)
        
        if len(data) < 2:
            data = data[0]
        
        if cmd == "":
            print("disconnected", sock)
            break
        
        elif cmd == "LOG":
            to_ret = login(*data)

        elif cmd == "GET":
            to_ret = handle_get(sock)
        
        
        elif cmd == "LOC":
            ADDRESS, *to_ret = handle_loc(data)
            to_ret = to_ret[0]
        
        elif cmd == "RAT":
            lock.acquire()
            to_ret = handle_rating(*data)
            lock.release()
        
        elif cmd == "REG":
            lock.acquire()
            to_ret = register(*data)
            lock.release()
            
          
        
        elif cmd == "PRD":
            print(data)
            jsonOBJ = json.loads(data)
            PRODUCTS = jsonOBJ['data']
            
            continue
                        
        elif cmd == "DAT":
            print(ADDRESS, *[d for d in data], PRODUCTS)
            result = get_shop_list(sock,ADDRESS, *[float(d) for d in data], PRODUCTS)
            json_string = json.dumps(result,ensure_ascii=False).encode()
            to_ret = send_by_chunks(json_string, 4,server,sock)
            

                
        print("sending",to_ret)
        server.send(*to_ret, sock=sock)
   
    
def main():
    while True:
        print("waiting in accept")
        sock,addr = server.accept()
        print("welcome!", addr)

        thread = threading.Thread(target=handle_client, args=(sock,))
        thread.start()
        threads.append(thread)


if __name__ == "__main__":
    main()

