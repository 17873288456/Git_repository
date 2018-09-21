package com.wenge.datagroup.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.wenge.datagroup.model.CrawlerHomeUrl;
import com.wenge.datagroup.model.CrawlerListUrl;

public interface RmiService extends Remote {
	public static Object lock = new Object();
	public static Object lock2 = new Object();
    public CrawlerHomeUrl getHomeUrl() throws RemoteException;
    public CrawlerListUrl getListUrl() throws RemoteException;
    public int getProxyPort() throws RemoteException;
    public String getProxyIP() throws RemoteException;
    public void updateCursorChannal(String id) throws RemoteException;
    public void updateCursorList(String id) throws RemoteException ;
}
