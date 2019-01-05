package com.sujit.test.ycatest;

import yjava.security.yca.CertDatabase;
import yjava.security.yca.Cert;
import yjava.security.yca.YCAException;

/**
 * Hello world!
 *
 */
public class App 
{

    public static void main( String[] args ) throws YCAException
    {
    	String appid;
        String ycaCertificate;
        CertDatabase certDB;
        certDB = new CertDatabase();
        ycaCertificate = certDB.getCert("yahoo.cp.yca.carmot_api-r.env-soln-stage");
        Cert certificate = new Cert(ycaCertificate);
        System.out.println(ycaCertificate);
        System.out.println(certificate.getPeerAppid());
    }
}
