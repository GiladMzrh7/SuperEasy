package com.example.yossi.setevent;


public class SessionData {
        private static String username;
        public static synchronized String  getUsername(){
            return username;
        }
        public static synchronized void setUsername(String username){ SessionData.username = username;}
        }

