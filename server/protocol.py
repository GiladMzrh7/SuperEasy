import socket
from TcpEncryptedSocket import EncSocket




def send(*msg, sock:socket.socket):
    
    new_msg = b''
    for i in msg:
        try:
            new_msg += i.encode() + b'~'
        except AttributeError:
             new_msg += i + b'~'

    new_msg = new_msg[:-1]
    new_msg = str(len(new_msg)).zfill(8).encode() + b'|' + new_msg

    sock.send(new_msg)
    print("sent", new_msg)



def send_by_chunks(jsn,chunk_amn:int , server:EncSocket, sock:socket.socket):
    chunk_size = len(jsn)//chunk_amn
    left = len(jsn) % chunk_amn
    
    server.send("CHL".encode(), str(chunk_size), sock=sock)
    cmd,data = server.recieve(sock)
    for i in range(chunk_amn):
        server.send("CHD".encode(),jsn[i * chunk_size: (i+1) * chunk_size], sock=sock)
        server.recieve(sock)

    
    return "FNS", jsn[len(jsn) - left:]


def recieve(sock:socket.socket) -> tuple[str, list[bytes, bytes]]:
    len_s = ""
    
    while len(len_s) < 8:
        try:
            len_s += sock.recv(8-len(len_s)).decode()
        except ConnectionResetError:
            print("disconnected" , sock.getpeername())
            return "", ""
        
        
    len_s = int(len_s)
    data = b""

    while(len(data) < len_s + 1):
        try:
            data += sock.recv(len_s+ 1 - len(data))
        except ConnectionResetError:
            print("disconnected" , sock.getpeername())
            return "", ""
    
    data = data[1:].replace(b'??',b"")
    data = data.split(b"~")
    cmd = data.pop(0).decode()
    return cmd, data