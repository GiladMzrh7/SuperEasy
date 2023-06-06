import sqlite3
import hashlib
from pandas import DataFrame

encoding = 'utf-8'
on_error = 'replace'

class Product(object):
    
    def __init__(self, name:str, quantity:float, shop_id:int, price:float, date_changed):
        """_summary_

        Args:
            name (_type_): Name of the product
            quantity (_type_): the amount of the Product
            shop_id (_type_): shop id that the product exists in
            price (_type_): price of the product
            date_changed (_type_): the date it was updated lastly
        """
        
        self.name = name
        self.product_id = None
        self.quantity = quantity
        self.shop_id = shop_id
        self.price = price
        self.date_changed=date_changed

    
    def __str__(self) -> str:
        return f'"{self.name}","{self.date_changed}"'



class Shop(object):
    def __init__(self,name,address):
        self.name = name
        self.address = address
        self.id = None
        self.recom = 0
        self.distance = 0
        self.price = 0
        self.rating = 0
    
    def __str__(self) -> str:
        return f"'{self.name},'{self.address}'"

       
class DB_Handler():
    def __init__(self):
        self.conn = None  # will store the DB connection
        self.cursor = None   # will store the DB connection cursor
    def open_DB(self):
        """
        will open DB file and put value in:
        self.conn (need DB file name)
        and self.cursor
        """
        self.conn = sqlite3.connect('data.db')
        self.current=self.conn.cursor()
        self.conn.execute('pragma encoding=UTF8')
        self.current.fetchall()

        
    def close_DB(self):
        self.conn.close()

    def commit(self):
        self.conn.commit()

    def GetAllProductsName(self):
        self.open_DB()
        sql = f"select Name from Products"
        res = self.conn.execute(sql)
        to_ret = [i[0].replace("'","").replace('"','') for i in res.fetchall()]

        self.close_DB()
        return to_ret
    
    def GetAllProductsCode(self):
        self.open_DB()
        sql = f"select ProductID from Products"
        res = self.conn.execute(sql)
        to_ret = [i[0] for i in res.fetchall()]
        
        self.close_DB()
        return to_ret
    
    def append_df(self,df:DataFrame, table_name):
        self.open_DB()
        
        try:
            df.to_sql(name=table_name, con=self.conn, if_exists='append', index=False)

        except Exception as e:
            print("ERROR", e)
        
        self.close_DB()
    
    
    def GetAllProductsByShop(self, shop:Shop):
        self.open_DB()
        sql = f"select * from Products where shopID='{shop.id}'"
        res = self.conn.execute(sql)
        to_ret = res.fetchall()
        self.close_DB()
        return to_ret

    def GetUsers(self):
        self.open_DB()
        usrs=[]


        self.close_DB()

        return usrs
 
    def insert_new_shop(self, shop:Shop):
        self.open_DB()

        sql = u"INSERT INTO Shops (Name,Address)"
        sql += u'VALUES("%s","%s");' % (shop.name, shop.address)

        self.current.execute(sql)
        self.commit()

        sql = u"select ShopID from shops where Name='%s'" %shop.name 
        
        res = self.current.execute(sql).fetchone()[0]

        self.close_DB()

        return int(res)

    def insert_new_price(self, product:Product):
        self.open_DB()
        sql="INSERT INTO Prices (ShopID , ProductID, Price)"

        sql+=f' VALUES("{product.shop_id}", "{product.product_id}", {product.price});'

        res =self.current.execute(sql)
        self.commit()
        self.close_DB()
        

    def get_all_shops(self):
        self.open_DB()
        sql = "SELECT Name from shops"
        res = self.conn.execute(sql).fetchall()
        res = [shop[0].strip() for shop in res]
        self.close_DB()
        return res

        
    def get_all_shops_data(self):
        self.open_DB()
        sql = "SELECT * from shops"
        res = self.conn.execute(sql).fetchall()
        to_ret = []
        for i in res:
            s = Shop(i[1].strip(),i[2].strip())
            s.id = i[0]
            to_ret.append(s)
        
        self.close_DB()
        return to_ret
    
    def get_id_from_name(self,name):
        self.open_DB()
        sql = f"SELECT ProductID from Products where Name='{name}'"
        res = self.current.execute(sql).fetchall()[0][0]
        self.close_DB()
        
        return res        

    def get_price_by_shop(self, shop:Shop, names):
        total_price = 0
        for name in names:
            id = self.get_id_from_name(name)
            self.open_DB()
            sql=f"SELECT Price from Prices where ProductID='{id}' and ShopID='{shop.id}'"
            res = self.current.execute(sql).fetchone()
            if not res:
                return None
            
            total_price += res[0]
                
            self.close_DB()
        return total_price

    def get_rating_by_shop(self, shop:Shop):
        self.open_DB()
        sql = f"SELECT Rating from Shops where ShopID='{shop.id}'"
        res = self.current.execute(sql).fetchall()[0][0]
        self.close_DB()
        
        return float(res)

    def get_rating_by_id(self, id):
        self.open_DB()
        sql = f"SELECT Rating from Shops where ShopID='{id}'"
        res = self.current.execute(sql).fetchall()[0][0]
        self.close_DB()
        
        return float(res)

    def get_rating_data_by_id(self, id):
        self.open_DB()
        sql = f"SELECT RatingNum, RatingSum from Shops where ShopID={id}"
        print(sql)
        res = self.current.execute(sql).fetchall()[0]
        print(res)
        self.close_DB()
        return res
    
    def set_rating_data_by_id(self, id, new_rating, new_sum, new_num):
        self.open_DB()
        sql = f"UPDATE Shops SET Rating = {new_rating}, RatingNum={new_num}, RatingSum={new_sum} WHERE id = 123;"
        res = self.current.execute(sql).fetchall()[0][0]
        self.close_DB()

    def get_shop_id_by_name_and_address(self, name, address):
        self.open_DB()
        sql = f"SELECT ShopID from Shops where Name='{name}' and Address='{address}'"
        print(sql)
        res = self.current.execute(sql).fetchall()[0][0]
        self.close_DB()
        return res


    def insert_new_product(self, product:Product):
        self.open_DB()
        sql="INSERT INTO Products (Name ,DateChanged)"

        sql+=f" VALUES("
        sql += str(product)
        sql += ");"

        self.current.execute(sql)
        
        self.commit()
        self.close_DB()


    def update_Product(self,product:Product):
        self.open_DB()

        sql = "UPDATE Products SET Price=%s,DateChanged='%s',Quantity=%s where Name='%s'"
        self.current.execute(sql,(product.price,product.date_changed, product.quantity, product.name))
        self.commit()
        
        self.close_DB()


    def login(self, uname:str, pwd:str):
        self.open_DB()
        sql = "SELECT password,salt from Users where Uname=?"
        res = self.conn.execute(sql,(uname))
        
        try:
            new_pwd, salt = res.fetchall()[0] # in case this is a wrong username
        except IndexError:
            return False
        
        print(salt)
        print(new_pwd)
        
        return hashlib.sha256((pwd + salt).encode()).hexdigest() == new_pwd


    def check_if_name_taken(self, name:str) -> bool:
        """
            Checking if user already has the name of the newly registering user

        Args:
            name (str): the name

        Returns:
            bool: if it exists or not
        """
        
        self.open_DB()
        sql = "SELECT * FROM Users WHERE Uname=?;"
        res = self.current.execute(sql,(name,))
        self.close_DB()
        
        return res != None


    def register(self, uname:str, pwd:str, salt:str) -> None:
        """_summary_
            registering user to the data base
        Args:
            uname (str): username of the user
            pwd (str): password of the user. not hashed yet!!
            salt (str): salt, used for hashing the password
        """
        
        if not self.check_if_name_taken(uname):
            return "ERR","NAME IS TAKEN"
        
        self.open_DB()
        sql = "INSERT INTO Users VALUES(?,?,?);"
        res = self.current.execute(sql,(uname, hashlib.sha256((pwd+salt).encode()).hexdigest(), salt))
        self.commit()
        self.close_DB()
        
        return "WEL","COME"


