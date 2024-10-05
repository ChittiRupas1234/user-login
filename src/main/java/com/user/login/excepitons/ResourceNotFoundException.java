package com.user.login.excepitons;

public class ResourceNotFoundException extends Exception{

    public ResourceNotFoundException(String str){
        super(str);
    }

    public void Exceptions(String strs){
        ResourceNotFoundException rne=new ResourceNotFoundException(strs);
    }
}
