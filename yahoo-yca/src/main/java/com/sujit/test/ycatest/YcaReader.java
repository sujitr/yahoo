package com.sujit.test.ycatest;

import yjava.security.yca.CertDatabase;
import yjava.security.yca.Cert;
import yjava.security.yca.YCAException;

/**
 * Class to check the yca read functionalities in Yahoo based systems
 *
 */
public class YcaReader 
{

    public static void main( String[] args ) throws YCAException{
        String ycaCertificate;
        CertDatabase certDB;
        certDB = new CertDatabase();
        ycaCertificate = certDB.getCert("yahoo.cp.yca.carmot_api-r.env-soln-stage");
        Cert certificate = new Cert(ycaCertificate);
        System.out.println(ycaCertificate);
        System.out.println(certificate.getPeerAppid());
    }
    
    public static String getYcaCertificateValue(String appId) throws YCAException{
		String ycaCertificate = "";
		CertDatabase certDB;
		certDB = new CertDatabase();
		ycaCertificate = certDB.getCert(appId);
    	return ycaCertificate;
    }
}
