from SQL_ORM import *
from selenium import webdriver
from selenium.webdriver.common.by import By
import time
import requests
import zipfile
from io import BytesIO
import xml.etree.ElementTree as ET
import threading
import pandas as pd
from selenium.webdriver.common.keys import Keys
import gzip
from bs4 import BeautifulSoup
import codecs

downloadurl = "/Download.aspx?FileNm="

sites = {}
lock = threading.Lock()
PRODUCTS:list[pd.DataFrame] = []
PRICES:list[pd.DataFrame] = []

      
def load_data():
    """
    input:
        the informations will be like this:
        baseurl-mainpage-shopname
        for example: ynet.co.il-/home/0,7340,L-8,00.html-אתר ווינט

    output:
        sites dictonary will present the site_url as a key and main site address + site name as value
        for ex. {'shufersal.co.il':[/index.html, שופרסל]}
    """

    global sites

    with codecs.open("sites.txt", 'r', 'utf-8') as f:
        info = f.read().split("\n")

    for i in info:
        site_end = i.split('-')
        print(site_end)
        sites[site_end[0]] = (site_end[1] if site_end[1] != None else "", site_end[2])


def get_xml_data(url: str) -> list:
    """_summary_

    Args:
        url (str): the url of the site as it presents in the sites dict

    Returns:
        [xml_file(str), address(str)]: a list of each xml data file that presents the each shop, and the address of it
    """

    driver = webdriver.Chrome()
    driver.get(url + sites[url][0]) # loading the main site
    
    time.sleep(5) #sleeping to let js load

    data = []
    done = 0
    i = 1
    while done < 40:
        if i > 999:
            break
        p_element = driver.find_element(By.ID, f'tr{i}').text 
        
        arr = p_element.split(" ") # finding and getting each file
        name = arr.pop(0)
        i+=1    
        if 'Price' not in name:
            continue

        address = " ".join(arr).split("מחירים")[0]


        file_url = requests.get(url + downloadurl + name).text.split('"')[3]

        gzipped = requests.get(file_url).content

        z = zipfile.ZipFile(BytesIO(gzipped))
        xml_data = z.read(name.split(".")[0] + ".xml").decode()

        data.append((xml_data, address))
        done += 1

    return data


def get_my_index():
    lock.acquire()
    PRICES.append(pd.DataFrame({"ShopID":[], "Name":[], "Price":[]}))
    PRODUCTS.append(pd.DataFrame({ "Name":[], "DateChanged":[]}))
    index = len(PRICES) - 1
    lock.release()
    
    return index

    
def handle_shop(items, STORE_ID): 

    index = get_my_index()
    
    product_data = {"Name":[], "DateChanged":[]}
    price_data = { "ShopID":[],"Name":[], "Price":[]}
    
    for product in items:
        name:str = product.find("ItemNm").text.replace('"','').replace("'","")
        
        price = product.find("ItemPrice").text
        date_changed = product.find("PriceUpdateDate").text.split()[0]
        
        
        
        product_data["DateChanged"].append(date_changed)
        product_data["Name"].append(name)
        
        
        price_data["Price"].append(price)
        price_data["Name"].append(name)
        price_data["ShopID"].append(f'{STORE_ID}')

        

    
    PRICES[index] = DataFrame(price_data)
    
    PRODUCTS[index] = DataFrame(product_data)


def handle_shufersal_xml():
    page_url = "http://prices.shufersal.co.il/?page="    
    data = []
    
    for page in range(7):
        response = requests.get(f'{page_url}{page}')   
        soup = BeautifulSoup(response.content, "html.parser")
        
        div = soup.find("div", {"class": "webgrid"})
        table = div.find("table")
        tbody = table.find("tbody")
        rows = tbody.find_all("tr")
        
        for row in rows:
            td = row.find_all("td")
            link_url = td[0].find("a")["href"]
            try:
                address =  td[5].text.split("-")
                address[0] = address[0] + "שופרסל"
                address = " ".join(address)
            except:
                continue
            file_response = requests.get(link_url).content
            gzipper = gzip.GzipFile(fileobj=BytesIO(file_response))
            uncompressed_data = gzipper.read()
            
            data.append(uncompressed_data, address)
        
    return data


def insert_shufersal():
    thr = []
    handler_db = DB_Handler()

    shops_info = handle_shufersal_xml()

    for file, address in shops_info:
        tree = ET.fromstring(file)
        items = tree[-1]
        address:str = address.replace('"','').replace("'","")
        shop = Shop("שופרסל", address)
        
        lock.acquire()
        try:
            STORE_ID = handler_db.insert_new_shop(shop)
        except Exception as e:
            print(e)
            lock.release()
            continue
        
        lock.release()

        shop.id = STORE_ID
        t = threading.Thread(target=handle_shop, args=(items,STORE_ID))
        t.start()
        thr.append(t)
    
    for i in thr:
        i.join()


def handle_matrix():
    response = requests.get("http://matrixcatalog.co.il/NBCompetitionRegulations.aspx")
    soup = BeautifulSoup(response.content, "html.parser")
    
    mainurl = "http://matrixcatalog.co.il/"
    
    data = []
    
    with open("a.txt", 'wb') as f:
        f.write(response.content)
    
    div = soup.find("div", {"id": "pageWithoutFooter"})
    div2 = div.find("div", {"id": "competitionRegulations"})
    div3 = div2.find("div", {"id": "MainContent_content"})
    div4 = div3.find("div", {"id": "download_content"})
    table = div.find("table")
    rows = table.find_all("tr")
    rows.remove(rows[0])
    for row in rows:
        td = row.find_all("td")
        if "Promo" in td[3].text:
            continue
        
        link_url = td[-1].find("a")["href"].replace("\\","/")
        try:
            address =  td[3].text
            shop_name = td[2].text
            
        except Exception as e:
            print(e)
            continue
        
        file_response = requests.get(mainurl+link_url).content
        gzipper = gzip.GzipFile(fileobj=BytesIO(file_response))
        
        uncompressed_data = gzipper.read()
        data.append((uncompressed_data, address, shop_name))

    
def handle_all_shops_data():
    normal = get_xml_data()
    shops = []
     
    
def handle_url(url:str):
    """
    gets a comapny's url and delivers each shop xml_info and address and adds it to DB

    Args:
        url (str): url of site as he presents in sites dict
    
    output:
        update in servers.db
    """

    shops_info = get_xml_data(url)
    
    thr = []
    handler_db = DB_Handler()

    
    for file, address in shops_info:
        tree = ET.fromstring(file)
        items = tree[-1]
        address:str = address.replace('"','').replace("'","")
        shop = Shop(sites[url][1], address)
        print(sites[url][1])
        
        lock.acquire()
        try:
            STORE_ID = handler_db.insert_new_shop(shop)
        except Exception as e:
            print(e)
            lock.release()
            continue
        
        lock.release()

        shop.id = STORE_ID
        t = threading.Thread(target=handle_shop, args=(items,STORE_ID))
        t.start()
        thr.append(t)
    
    for i in thr:
        i.join()
        
        
def get_xml_with_login(login_url, username, pwd):
    driver = webdriver.Chrome()

    downloadurl = "'https://url.publishedprices.co.il/file/d/"
    
    # navigate to login page
    driver.get(login_url)

    # fill in login form
    username_input = driver.find_element( By.NAME,'username')
    password_input = driver.find_element(By.NAME, 'password')
    username_input.send_keys(username)
    password_input.send_keys(pwd)
    password_input.send_keys(Keys.RETURN)

    # wait for login to complete
    time.sleep(5)
    
    print(driver.get_cookies())
    
    session_cookie = driver.get_cookie('cftpSID')
    # navigate to file download page
    driver.get('https://url.publishedprices.co.il/file')

    # get file contents without downloading file
    file_url = 'https://url.publishedprices.co.il/file/d/Price7290492000005-450-202305050700.gz'
    session = requests.Session()
    session.cookies.set("cftpSID", session_cookie['value'])
    response = session.get(file_url)
    file_contents = response.content
    print(len(file_contents))
    a = BytesIO(file_contents)

    with open("a.gz",'rb') as f:
        data = f.read()
    
    assert data == file_contents

    gzipper = gzip.GzipFile(fileobj=a)
    uncompressed_data = gzipper.read()
    

    # close the webdriver
    driver.quit()

def main():
    load_data()
    handler_db = DB_Handler()
    threads = []
    for i,v in enumerate(sites):
        t = threading.Thread(target=handle_url, args=(v,))
        t.start()
        threads.append(t)

    
    t = threading.Thread(target=handle_shufersal_xml)
    t.start()
    threads.append(t)
        
    for t in threads:
        t.join()
        
    FINAL_PRODUCTS = pd.concat(PRODUCTS).drop_duplicates(subset='Name')
    FINAL_PRODUCTS.insert(0, 'ProductID', range(len(FINAL_PRODUCTS)))
    
    print(FINAL_PRODUCTS)
    
    FINAL_PRICES = pd.concat(PRICES)
    print(FINAL_PRICES) 
    handler_db.append_df(FINAL_PRODUCTS, "Products")
    
    mergedDF = pd.merge(FINAL_PRODUCTS[['ProductID', 'Name']], FINAL_PRICES, on='Name')
    
    print(mergedDF.columns)
    
    mergedDF.drop("Name", axis=1,inplace=True)
    mergedDF.drop_duplicates(subset=['ProductID', 'ShopID'], inplace=True)

    handler_db.append_df(mergedDF, "Prices")

def check():
    load_data()
    handle_url("http://shefabirkathashem.binaprojects.com")
        

if __name__ == "__main__":
    main()
