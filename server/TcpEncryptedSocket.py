import socket
import random
import string
import pickle
from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP
import logging
import base64
import datetime
import os
from Crypto.Cipher import AES
import threading

def encrypt(message:str, key:str):
    """Encrypts a message using the given key.

  Args:
    message: The message to encrypt.
    key: The key to use for encryption.

  Returns:
    The encrypted message and IV, separated by the "|" character.
  """

    iv = os.urandom(16) #randomizing 16 bytes
    
    # making sure the message and the key are both bytes
    try:
        message = message.encode()
    except:
        pass
    
    try:
        key = key.encode()
    except:
        pass
    
    # padding the message so it will fit aes 16 byte encryption
    padded_message = _pad(message)
    
    # creating cipher object.
    cipher = AES.new(key, AES.MODE_CBC, iv)
    
    encrypted_message = cipher.encrypt(padded_message)
    
    # adding the iv and encrypted message with the "?|" chars.
    iv_and_encrypted_message = iv + b"?|" +encrypted_message
    
    # Base64-encode the result and return it as a string.
    return base64.b64encode(iv_and_encrypted_message)

def decrypt(encrypted_message:str, key:str):
    
    enc_msg = base64.b64decode(encrypted_message)
    #decoding the base64

    iv, encoded = enc_msg.split(b"?|", maxsplit=1)
    #the initial value is based on the first bytes of the string before the "?|"

    cipher = AES.new(key, AES.MODE_CBC, iv)
    decrypted = cipher.decrypt(encoded)
    return _unpad(decrypted.decode('utf-8'))

def _pad(message):
    padding_length = 16 - (len(message) % 16)
    padding = bytes([padding_length]) * padding_length
    return message + padding

def _unpad(message):
    padding_length = ord(message[-1])
    return message[:-padding_length]


def key_gen(size=8, chars=string.ascii_uppercase + string.digits + string.ascii_lowercase):
    """
    generating keys by fixed size and chars

    Args:
        size (int, optional): the size of the key. Defaults to 8.\n
        chars (list, optional): the chars that will be ranomize in the key. Defaults to string.ascii_uppercase+string.digits+string.ascii_lowercase.

    Returns:
        str: the key
    """
    return ''.join(random.choice(chars) for _ in range(size))


class EncSocket(socket.socket):
    
    """
        class that inherits from socket that does encryption both on RSA and AES
        AES with diffie-hellman protocol
        RSA with PyCrypto
    """
    threads = []
    dh_aes = 'dh_aes'
    rsa = 'rsa'
    MSG_SIZE = 8
    TAV_MAFRID = "#@@!?#%@"

    def __init__(self, addr, client=True, protocol=dh_aes) -> None:
        super().__init__()
        logging.basicConfig(filename='server_log.log',level=logging.DEBUG)
        self.client = client
        self.addr = addr

        if protocol and protocol != self.dh_aes and protocol != self.rsa:
            raise ValueError(f"\nERROR:\nUnknown protocol: {protocol} \npls use Encsocket.rsa or dh_aes")

        self.prot = protocol

        if self.prot == self.dh_aes:
            self.my_key = int(key_gen(chars=string.digits))

        elif self.prot == self.rsa:
            self.key_pair = RSA.generate(2048)
            self.my_key = self.key_pair.publickey()

        if not client:
            self.public_codes = {}

            self.prime = 641928468969180888504879500549

            self.gen = int(key_gen(size=32, chars=string.digits))
            self.bind(self.addr)
            self.listen()
            self.protocols = {}

            self.my_key_dh_aes = int(key_gen(chars=string.digits))

            self.key_pair = RSA.generate(2048)
            self.my_key_rsa = self.key_pair.publickey()


    def accept(self) -> tuple:
        
        c, addr = super().accept() #accepting the socket
        print(addr)
        
        prot = self.__recieve_diffie(c) # recieving protocol
        
        self.protocols[c] = prot
        if prot == self.dh_aes:
            self.__diffie_accept(c)
            
        elif prot == self.rsa:
           self.__rsa_accept(c)
        
        return c,addr
        
    def __diffie_accept(self, c):

        #sending global prime number
        self.__send_diffie(c,"PRM", str(self.prime)) 

        self.__recieve_diffie(c)  # ack

        #sending server's geneartor
        gen = random.randint(1, self.prime - 2)
        self.__send_diffie(c, "GEN",str(gen))

        #reciving client's number
        other_num = int(self.__recieve_diffie(c))
        
        mynum = pow(gen, self.my_key, self.prime) #diffie number

        #sending my result
        msg = str(mynum)
        self.__send_diffie(c, "MNM",msg)


        #finising the diffie hellman
        final_num = str(pow(other_num, self.my_key, self.prime))[:24]

        self.public_codes[c] = final_num[:24] 

    def __rsa_accept(self, c):
        a = self.__revieve_pickle_normal(c)
        self.__send_pickle_normal(self.my_key_rsa.export_key(), c)
        self.public_codes[c] = RSA.import_key(a)

    def __recieve_diffie(self, sock):
        leng = b''
        while len(leng) < self.MSG_SIZE + 2:
            leng += sock.recv(1)
            
        data = b''
        while len(data) < int(leng[:-2]):
            try:
                chunk = sock.recv(1)
                data += chunk

                if chunk == b'':  # if got nothing, that means that socket disconnected
                    return None, None, None

            except ConnectionResetError:
                return None, None, None  # socket was closed for some other reason

        data = data.decode()
        return data

    def __send_diffie(self, sock:socket.socket, cmd:str,data:str):

        
        print(str(len(data+cmd+self.TAV_MAFRID)).zfill(self.MSG_SIZE).encode() + (cmd + self.TAV_MAFRID + data).encode())
        if type(sock) == EncSocket:
            super().send(str(len(data+cmd+self.TAV_MAFRID)).zfill(self.MSG_SIZE).encode() +("|"+ cmd + self.TAV_MAFRID + data).encode())
        else:
            sock.send(str(len(data+cmd+self.TAV_MAFRID)).zfill(self.MSG_SIZE).encode() + ("|"+ cmd + self.TAV_MAFRID + data).encode())
            

    def __send_normal(self, args, sock=None):

        msg = b''
        for i in args:
            try:
                i = i.encode()
            except AttributeError:
                pass
            msg += i + self.TAV_MAFRID.encode()
        msg += "##".encode()

        if not self.client:
            sock.send(str(len(msg)).zfill(self.MSG_SIZE).encode() + msg)
        else:
            super().send(str(len(msg)).zfill(self.MSG_SIZE).encode() + msg)

    def recieve(self, sock=None):
        print("here")
        if not self.client:
            prot = self.protocols[sock]
        else:
            prot = self.prot

        if not sock:
            sock = self

        if not prot:
            return self.__recieve_msg_normal(sock)

        elif prot == self.dh_aes:
            data = self.__recieve_dh_aes(sock)
        elif prot == self.rsa:
            data = self.__recieve_msg_rsa(sock)
        if not self.client:
            return data

        return data

    def __recieve_dh_aes(self, sock: socket.socket):
        if self.client:
            key = self.public_key
        else:
            key = self.public_codes[sock]

        sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
        sock.settimeout(10000)

        data = self.__get_bdata(sock).decode()
        print("recieved:",data)
        data = data.split(self.TAV_MAFRID)  # cleaning the data
        
        data = list(filter(None, data))
        to_ret = []
        for i in data:
            to_ret.append(decrypt(i.encode(),key.encode()))

        return to_ret

    def __send_msg_aes(self, args, sock=None):
        if self.client:
            key = self.public_key
        else:
            key = self.public_codes[sock]

        msg = b''
        first = True
        for i in args:
            if not first:
                msg += self.TAV_MAFRID.encode()
            else:
                first = False
                
            encoded = encrypt(i,key)
            print("sending",encoded)
            msg += encoded

        if sock != self:
            sock.send(str(len(msg)).zfill(8).encode() + msg)
        else:
            super().send(str(len(msg)).zfill(8).encode() + msg)
            

    def __send_msg_rsa(self, args, sock=None):
        if not self.client:
            key = self.public_codes[sock]
        else:
            key = self.public_key

        msg = b''
        for i in args:
            encryptor = PKCS1_OAEP.new(key)
            try:
                i = i.encode()
            except AttributeError:
                pass
            d = encryptor.encrypt(i)
            msg += d + self.TAV_MAFRID.encode()

        if not self.client:
            sock.send(str(len(msg)).zfill(self.MSG_SIZE).encode() + msg)
        else:
            super().send(str(len(msg)).zfill(self.MSG_SIZE).encode() + msg)

    def send(self, *args, sock=None):        
        if self.client:
            prot = self.prot
        else:
            prot = self.protocols[sock]

        if not sock:
            sock = self

        if not prot:
            self.__send_normal(args)

        elif prot == self.dh_aes:
            self.__send_msg_aes(args, sock=sock)
        elif prot == self.rsa:
            self.__send_msg_rsa(args, sock=sock)


    def __recieve_msg_rsa(self, sock=None):
        key = self.key_pair

        dec = PKCS1_OAEP.new(key)

        data = self.__get_bdata(sock)

        # write_to_log(sock.getpeername(),data)
        data = data.split(self.TAV_MAFRID.encode())  # cleaning the data
        data = data[:-1]
        data = list(filter(None, data))

        to_ret = []
        for i in data:
            to_ret.append(dec.decrypt(i).decode())

        return to_ret

    def __recieve_msg_normal(self, sock=None):

        data = self.__get_bdata(sock)

        # write_to_log(sock.getpeername(),data)
        data = data.split(self.TAV_MAFRID.encode())  # cleaning the data
        data = data[:-1]
        data = list(filter(None, data))

        to_ret = []
        for i in data:
            to_ret.append(i.decode())

        return to_ret



        key = self.key_pair

        dec = PKCS1_OAEP.new(key)

        leng = b''
        while len(leng) < self.MSG_SIZE:
            leng += s.recv(1)

        data = b''
        while len(data) < int(leng):
            data += s.recv(1)

        bdata = dec.decrypt(data)

        return pickle.loads(bdata)


        if not s:
            s = self

        leng = b''
        while len(leng) < self.MSG_SIZE:
            leng += s.recv(1)

        data = b''
        while len(data) < int(leng):
            data += s.recv(1)

        return pickle.loads(data)

        prot = self.prot if self.client else self.protocols[sock]

        if prot == self.dh_aes:
            return self.__send_pickle_dh_aes(obj, sock)
        elif prot == self.rsa:
            return self.__send_pickle_rsa(obj, sock)

    def __get_bdata(self, sock):
        leng = b''
        while len(leng) < self.MSG_SIZE + 2:
            leng += sock.recv(1)

        data = b''
        leng = leng[:-2]

        while len(data) < int(leng):
            try:
                chunk = sock.recv(1)
                data += chunk

            except ConnectionResetError:
                return None, None, None  # socket was closed for some other reason
            except socket.timeout:
                return None, None, None

            except Exception:
                continue
        return data


        sock.settimeout(time)
        data = self.__get_bdata(sock)

        data = data.split(self.TAV_MAFRID.encode())  # cleaning the data
        data = data[:-1]
        data = list(filter(None, data))

        to_ret = []
        for i in data:
            to_ret.append(i)

        return to_ret

        sender = sock.getpeername() if sock != self else 'SERVER'
        cmd = data.pop(0)
        if cmd == 'ERROR':
            logging.error(f'{datetime.datetime.now()}-{sender}: {cmd} {str(data)[1:-2]}')
        else:
            logging.debug(f'{datetime.datetime.now()}-{sender}: {cmd} {str(data)[1:-2]}')

