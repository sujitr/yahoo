package com.sujit.test.ycatest;

import yjava.security.yca.CertDatabase;
import yjava.security.yca.Cert;
import yjava.security.yca.YCAException;

/**
 * Class to read yca functionalities in Yahoo based systems
 * @author sujitroy
 */
public class YcaReader 
{

	/**
	 * Main class for local test purposes
	 * @param args
	 * @throws YCAException
	 */
    public static void main( String[] args ) throws YCAException{
        String ycaCertificate;
        CertDatabase certDB;
        certDB = new CertDatabase();
        ycaCertificate = certDB.getCert("yahoo.cp.yca.carmot_api-r.env-soln-stage");
        Cert certificate = new Cert(ycaCertificate);
        System.out.println(ycaCertificate);
        System.out.println(certificate.getPeerAppid());
    }
    
    /**
     * Static method to make direct call to get the yca value for a given yahoo app id
     * @param appId
     * @return
     * @throws YCAException
     */
    public static String getYcaCertificateValue(String appId) throws YCAException{
		String ycaCertificate = "";
		CertDatabase certDB;
		certDB = new CertDatabase();
		ycaCertificate = certDB.getCert(appId);
    	return ycaCertificate;
    }
}
