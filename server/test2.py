from SQL_ORM import *
import googlemaps
import datetime
import threading

handler = DB_Handler()
API_KEY = "AIzaSyDL2r9jQeu6oENdZAosopyt7RGpDTktQfA"
google = googlemaps.Client(API_KEY)

ADDRESS = 'אז"ר 49,כפר סבא, ישראל'
cheap = 50
near = 50
good = 50
MAX_DIST = 15000
CLIENT_DATA = {}

def handle_distances(client_address, shops):
    addresses = [shop.address for shop in shops]

    done = 0
    while done < len(shops) - 1:
        distance_data = google.distance_matrix(client_address, addresses[done:done +25])['rows'][0]

        for dic in distance_data['elements']:
            distance = dic

            if distance['status'] != 'OK':
                continue

            result = distance['distance']
            print(result)
            CLIENT_DATA[client_address]['addresses'].append(int(result['value']))
            done += 1
    
    CLIENT_DATA[client_address]['done'] = True

def get_shop_list(client_address:str, near:float, cheap:float, good:float, products:list) -> list:
    """THE MAIN METHOD OF THE PROJCET!
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
    
    CLIENT_DATA[client_address] = {"removed":[], "done":False, "addresses":[]}
    
    shops = handler.get_all_shops_data()
    
    t = threading.Thread(target=handle_distances, args=(client_address,shops))
    t.start()
    
    allowed = []
    max_allowed = near * MAX_DIST

    for i,shop in enumerate(shops):
        shop.recom = 0
        shop.price = handler.get_price_by_shop(shop,products)

        if not shop.price:
            CLIENT_DATA[client_address]['removed'].append(i)
            continue
        
        shop.recom += 33 #start with everyone are good
        allowed.append(shop)

    prices = sorted(allowed, key = lambda x: x.price)

    lowest_price = prices[0].price

    for i,v in enumerate(prices):
        if i in  CLIENT_DATA[client_address]['removed']: continue
        shop.recom += 33 * (lowest_price/v.price)
    
    for i,s in enumerate(shops):
        while not CLIENT_DATA[client_address]['done']:
            continue
        
        if i in  CLIENT_DATA[client_address]['removed']:
            continue
        
        distance = CLIENT_DATA[client_address]['addresses'][i]
        s.distance = distance
        if distance < max_allowed:
            s.recom += 33
        
        elif distance >= MAX_DIST:
            continue
        
        else:
            pct = distance/max_allowed
            s.recom += 33 * pct
        
    sort:list[Shop] =  sorted(allowed,key= lambda x: x.recom, reverse=True)
    
    t.join()    
    return [[s.name, s.address,s.price, s.recom] for s in sort]


d = get_shop_list(ADDRESS, near, cheap, good, ['פלפל ירוק'])
print(d[0][0])
print(d[0][1])
print(d[0][2])
print(d[0][3])