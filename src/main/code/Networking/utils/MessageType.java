package main.code.Networking.utils;

public enum MessageType {
    PLAYER , HANDSHAKE , ROOMS, MISC, ADDOUTPUTSTREAM,HANDSHAKEOK,BULLETSFIRED, CONNECTTOGAME,ROUNDOVER,AVAILABLEGAMESMAX,GAMEOVER,CONNECTIONTOGAMEFAILED,HANDSHAKEAFTERGAME ,CREATEGAME,GAMECREATED, CONNECTEDTOGAME,ADDUSER ,SERVERFULL,CLIENT_READY,CLOSECONNECTION,USERADDED,GAMECREATIONFAILED,PLAYERS,FAILUSERNAME,UPDATEBOXES, DISCONNECTFROMGAME,DISCONNECTEDFROMGAME, AVAILABLEGAMES, DISCONNECT, DISCONNECTED, CLIENT_NOT_READY, GETPLAYERS, SETSPAWN;

    public static MessageType getType(byte [] packet) {
        if(new String(packet).contains("GAMEOVER")) return GAMEOVER;

        else if(new String(packet).contains("ROUNDOVER")) return ROUNDOVER;
        else if(new String(packet).contains("GETPLAYERS")) return GETPLAYERS;
        else if(new String(packet).contains("BULLETSFIRED")) return BULLETSFIRED;
        else if(new String(packet).contains("HANDSHAKEAFTERGAME")) return HANDSHAKEAFTERGAME;
        else if(new String(packet).contains("SETSPAWN")) return SETSPAWN;
        else if(new String(packet).contains("PLAYERS")) return PLAYERS;
        else if(new String(packet).contains("DISCONNECTEDFROMGAME")) return DISCONNECTEDFROMGAME;
        else if(new String(packet).contains("DISCONNECTFROMGAME")) return DISCONNECTFROMGAME;
        else if(new String(packet).contains("DISCONNECTED")) return DISCONNECTED;
        else if(new String(packet).contains("DISCONNECT")) return DISCONNECT;
        else if(new String(packet).contains("AVAILABLEGAMESMAX")) return AVAILABLEGAMESMAX;

        else if(new String(packet).contains("AVAILABLEGAMES")) return AVAILABLEGAMES;
        else if(new String(packet).contains("PLAYER")) return PLAYER;
        else if(new String(packet).contains("HANDSHAKEOK")) return HANDSHAKEOK;
        else if(new String(packet).contains("HANDSHAKE")) return HANDSHAKE;
        else if(new String(packet).contains("ROOMS")) return ROOMS;
        else if(new String(packet).contains("CONNECTTOGAME")) return CONNECTTOGAME;
        else if(new String(packet).contains("CREATEGAME")) return CREATEGAME;
        else if(new String(packet).contains("GAMECREATED")) return GAMECREATED;
        else if(new String(packet).contains("CONNECTEDTOGAME")) return CONNECTEDTOGAME;
        else if(new String(packet).contains("GAMECREATIONFAILED")) return GAMECREATIONFAILED;
        else if(new String(packet).contains("UPDATEBOXES")) return UPDATEBOXES;
        else if(new String(packet).contains("FAILUSERNAME")) return FAILUSERNAME;
        else if(new String(packet).contains("ADDUSER")) return ADDUSER;
        else if(new String(packet).contains("USERADDED")) return USERADDED;
        else if(new String(packet).contains("CLOSECONNECTION")) return CLOSECONNECTION;
        else if(new String(packet).contains("SERVERFULL")) return SERVERFULL;
        else if(new String(packet).contains("CLIENT_READY")) return CLIENT_READY;
        else if(new String(packet).contains("ADDOUTPUTSTREAM")) return ADDOUTPUTSTREAM;
        else if(new String(packet).contains("CLIENT_NOT_READY")) return CLIENT_NOT_READY;
        else if(new String(packet).contains("CONNECTIONTOGAMEFAILED")) return CONNECTIONTOGAMEFAILED;

        else return MISC;
    }}
