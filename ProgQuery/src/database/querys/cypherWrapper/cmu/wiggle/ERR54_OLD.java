package database.querys.cypherWrapper.cmu.wiggle;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.UnwindClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.services.MethodInvocationServicesWiggle;
import database.querys.services.PDGServicesWiggle;
import database.querys.services.TypeServicesWiggle;

public class ERR54_OLD extends AbstractQuery {

	/*
	 * " MATCH (closeableSubtype)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*0..]->(closeableInt:INTERFACE_DECLARATION{fullyQualifiedName:'java.lang.AutoCloseable'})"
	 * +
	 * " WHERE closeableSubtype:CLASS_DECLARATION OR closeableSubtype:INTERFACE_DECLARATION "
	 * + " WITH DISTINCT closeableSubtype.fullyQualifiedName as className "
	 * 
	 * +
	 * " MATCH (closeableDec{actualType:className})-[:MODIFIED_BY]->(assign) <-["
	 * + assignToOutExprQuery + "*0..]-(expr)<-[" + exprToStatQuery +
	 * "]-(assignStat) " +
	 * 
	 * 
	 * 
	 * 
	 * " OPTIONAL MATCH (closeableDec)<-[r:TRY_RESOURCES]-()  WITH assignStat,r, closeableDec WHERE r IS NULL"
	 * 
	 * +
	 * "MATCH (mInv:METHOD_INVOCATION)-[:METHODINVOCATION_METHOD_SELECT]->(mSelect:MEMBER_SELECTION{memberName:'close'})-[:MEMBER_SELECT_EXPR]->(id)<-[:USED_BY]-(closeableDec),"
	 * + " (mInv)<-[" + exprToOutExprQuery + "*0..]-(expr)<-[" + exprToStatQuery
	 * + "]-(closeStat)" + ",
	 * 
	 * (assignStat)-[" + cfgSuccesor + "*1..]->(prev)" +
	 * "-[:IF_THERE_IS_UNCAUGHT_EXCEPTION | :MAY_THROW | :THROWS]->(afterEx)" +
	 * 
	 * " WITH  COLLECT(DISTINCT closeStat) AS closes, prev,closeableDec, afterEx"
	 * 
	 * 
	 * + " MATCH (prev)-[" + cfgSuccesor +
	 * "*]->(closeStat) WHERE closeStat IN closes" +
	 * 
	 * " WITH closes,COLLECT(DISTINCT prev) AS prevs, afterEx, closeableDec" +
	 * 
	 * " WITH closes, [prev IN prevs WHERE NOT prev IN closes] as prevs, afterEx , closeableDec"
	 * + " WHERE SIZE(prevs)>=1 " + " MATCH (afterEx)-[" +
	 * getAnyRel(toCFGSuccesorNoCondEx) +
	 * "*0..]->(reachableAfterEx) WITH closes,COLLECT(reachableAfterEx) AS reachable, prevs, closeableDec"
	 * + " WHERE ALL(r IN reachable WHERE NOT r IN closes)"
	 * 
	 * +
	 * " RETURN 'Warning [CMU-ERR54] variable '+closeableDec.name+ '(defined in line'+closeableDec.lineNumber+') might not be properly closed, as statement(s) (in lines '+ EXTRACT(prev IN prevs | prev.lineNumber)+') may throw an exception.'"
	 * 
	 * 
	 */
	private final String METHOD_THAT_THROWS = "[['java.lang.Thread','sleep','(long)',['java.lang.InterruptedException']]\r\n" + 
			",['java.io.BufferedReader','readLine','()',['java.io.IOException']]\r\n" + 
			",['java.lang.Double','valueOf','(java.lang.String)',['java.lang.NumberFormatException']]\r\n" + 
			",['java.lang.Integer','valueOf','(java.lang.String)',['java.lang.NumberFormatException']]\r\n" + 
			",['android.content.res.Resources','getStringArray','(int)',['android.content.res.Resources$NotFoundException']]\r\n" + 
			",['android.database.sqlite.SQLiteDatabase','execSQL','(java.lang.String)',['android.database.SQLException']]\r\n" + 
			",['android.media.MediaPlayer','start','()',['java.lang.IllegalStateException']]\r\n" + 
			",['android.media.MediaPlayer','stop','()',['java.lang.IllegalStateException']]\r\n" + 
			",['java.lang.Object','wait','()',['java.lang.InterruptedException']]\r\n" + 
			",['java.lang.Integer','parseInt','(java.lang.String)',['java.lang.NumberFormatException']]\r\n" + 
			",['java.lang.Integer','<init>','(java.lang.String)',['java.lang.NumberFormatException']]\r\n" + 
			",['org.apache.zookeeper.ZooKeeper','delete','(java.lang.String,int)',['java.lang.InterruptedException','org.apache.zookeeper.KeeperException']]\r\n" + 
			",['org.apache.zookeeper.ZooKeeper','create','(java.lang.String,byte[],java.util.List,org.apache.zookeeper.CreateMode)',['java.lang.InterruptedException','org.apache.zookeeper.KeeperException']]\r\n" + 
			",['org.apache.zookeeper.ZooKeeper','getData','(java.lang.String,boolean,org.apache.zookeeper.data.Stat)',['java.lang.InterruptedException','org.apache.zookeeper.KeeperException']]\r\n" + 
			",['org.apache.zookeeper.ZooKeeper','getChildren','(java.lang.String,boolean)',['java.lang.InterruptedException','org.apache.zookeeper.KeeperException']]\r\n" + 
			",['org.apache.zookeeper.ZooKeeper','exists','(java.lang.String,boolean)',['java.lang.InterruptedException','org.apache.zookeeper.KeeperException']]\r\n" + 
			",['org.apache.zookeeper.ZooKeeper','<init>','(java.lang.String,int,org.apache.zookeeper.Watcher)',['java.io.IOException']]\r\n" + 
			",['org.w3c.dom.Node','getNodeValue','()',['org.w3c.dom.DOMException']]\r\n" + 
			",['java.io.ObjectInputStream','readObject','()',['java.io.IOException','java.lang.ClassNotFoundException']]\r\n" + 
			",['java.io.ObjectInputStream','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectInputStream','<init>','(java.io.InputStream)',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectOutputStream','writeObject','(java.lang.Object)',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectOutputStream','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectOutputStream','<init>','(java.io.OutputStream)',['java.io.IOException']]\r\n" + 
			",['java.io.FileInputStream','<init>','(java.io.File)',['java.io.FileNotFoundException']]\r\n" + 
			",['java.io.FileInputStream','<init>','(java.lang.String)',['java.io.FileNotFoundException']]\r\n" + 
			",['java.nio.channels.FileChannel','tryLock','()',['java.io.IOException']]\r\n" + 
			",['java.io.FileOutputStream','write','(byte[])',['java.io.IOException']]\r\n" + 
			",['java.io.FileOutputStream','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.FileOutputStream','<init>','(java.io.File)',['java.io.FileNotFoundException']]\r\n" + 
			",['java.lang.reflect.Field','get','(java.lang.Object)',['java.lang.IllegalAccessException','java.lang.IllegalArgumentException']]\r\n" + 
			",['java.lang.reflect.Method','invoke','(java.lang.Object,java.lang.Object[])',['java.lang.IllegalAccessException','java.lang.IllegalArgumentException','java.lang.reflect.InvocationTargetException']]\r\n" + 
			",['java.lang.Long','parseLong','(java.lang.String)',['java.lang.NumberFormatException']]\r\n" + 
			",['java.net.InetAddress','getLocalHost','()',['java.net.UnknownHostException']]\r\n" + 
			",['java.net.InetAddress','getByName','(java.lang.String)',['java.net.UnknownHostException']]\r\n" + 
			",['java.lang.reflect.AccessibleObject','setAccessible','(boolean)',['java.lang.SecurityException']]\r\n" + 
			",['java.lang.reflect.Array','get','(java.lang.Object,int)',['java.lang.ArrayIndexOutOfBoundsException','java.lang.IllegalArgumentException']]\r\n" + 
			",['java.lang.reflect.Array','getLength','(java.lang.Object)',['java.lang.IllegalArgumentException']]\r\n" + 
			",['java.net.DatagramSocket','receive','(java.net.DatagramPacket)',['java.io.IOException']]\r\n" + 
			",['java.net.DatagramSocket','send','(java.net.DatagramPacket)',['java.io.IOException']]\r\n" + 
			",['java.net.DatagramSocket','setReceiveBufferSize','(int)',['java.net.SocketException']]\r\n" + 
			",['java.net.DatagramSocket','<init>','()',['java.net.SocketException']]\r\n" + 
			",['java.net.DatagramSocket','<init>','(int)',['java.net.SocketException']]\r\n" + 
			",['java.net.MulticastSocket','joinGroup','(java.net.InetAddress)',['java.io.IOException']]\r\n" + 
			",['java.net.MulticastSocket','<init>','()',['java.io.IOException']]\r\n" + 
			",['java.net.MulticastSocket','<init>','(int)',['java.io.IOException']]\r\n" + 
			",['javax.swing.JOptionPane','showConfirmDialog','(java.awt.Component,java.lang.Object,java.lang.String,int)',['java.awt.HeadlessException']]\r\n" + 
			",['javax.xml.parsers.DocumentBuilder','parse','(java.io.InputStream)',['java.io.IOException','org.xml.sax.SAXException']]\r\n" + 
			",['javax.xml.parsers.DocumentBuilderFactory','newDocumentBuilder','()',['javax.xml.parsers.ParserConfigurationException']]\r\n" + 
			",['java.lang.Class','getDeclaredFields','()',['java.lang.SecurityException']]\r\n" + 
			",['java.lang.Class','getMethods','()',['java.lang.SecurityException']]\r\n" + 
			",['java.lang.Object','wait','(long)',['java.lang.InterruptedException']]\r\n" + 
			",['java.io.Reader','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.Writer','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.Writer','flush','()',['java.io.IOException']]\r\n" + 
			",['java.io.Writer','write','(java.lang.String)',['java.io.IOException']]\r\n" + 
			",['java.lang.Class','newInstance','()',['java.lang.IllegalAccessException','java.lang.InstantiationException']]\r\n" + 
			",['java.lang.Class','getDeclaredMethod','(java.lang.String,java.lang.Class[])',['java.lang.NoSuchMethodException','java.lang.SecurityException']]\r\n" + 
			",['java.lang.Class','getConstructor','(java.lang.Class[])',['java.lang.NoSuchMethodException','java.lang.SecurityException']]\r\n" + 
			",['java.lang.Class','forName','(java.lang.String)',['java.lang.ClassNotFoundException']]\r\n" + 
			",['java.io.File','createNewFile','()',['java.io.IOException']]\r\n" + 
			",['javax.swing.JApplet','<init>','()',['java.awt.HeadlessException']]\r\n" + 
			",['javax.swing.JFrame','<init>','(java.lang.String)',['java.awt.HeadlessException']]\r\n" + 
			",['javax.swing.JFrame','<init>','()',['java.awt.HeadlessException']]\r\n" + 
			",['junit.framework.TestCase','setUp','()',['java.lang.Exception']]\r\n" + 
			",['junit.framework.TestCase','tearDown','()',['java.lang.Exception']]\r\n" + 
			",['java.io.BufferedReader','close','()',['java.io.IOException']]\r\n" + 
			",['java.net.ServerSocket','close','()',['java.io.IOException']]\r\n" + 
			",['java.net.ServerSocket','accept','()',['java.io.IOException']]\r\n" + 
			",['java.net.ServerSocket','<init>','(int)',['java.io.IOException']]\r\n" + 
			",['java.lang.Thread','join','()',['java.lang.InterruptedException']]\r\n" + 
			",['java.awt.Robot','<init>','()',['java.awt.AWTException']]\r\n" + 
			",['javax.swing.JInternalFrame','setSelected','(boolean)',['java.beans.PropertyVetoException']]\r\n" + 
			",['javax.swing.JInternalFrame','setClosed','(boolean)',['java.beans.PropertyVetoException']]\r\n" + 
			",['javax.swing.JFileChooser','showOpenDialog','(java.awt.Component)',['java.awt.HeadlessException']]\r\n" + 
			",['javax.swing.JFileChooser','showSaveDialog','(java.awt.Component)',['java.awt.HeadlessException']]\r\n" + 
			",['java.awt.Toolkit','getScreenSize','()',['java.awt.HeadlessException']]\r\n" + 
			",['java.awt.KeyboardFocusManager','setCurrentKeyboardFocusManager','(java.awt.KeyboardFocusManager)',['java.lang.SecurityException']]\r\n" + 
			",['java.awt.Frame','<init>','()',['java.awt.HeadlessException']]\r\n" + 
			",['java.lang.reflect.Field','getInt','(java.lang.Object)',['java.lang.IllegalAccessException','java.lang.IllegalArgumentException']]\r\n" + 
			",['java.io.BufferedWriter','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.BufferedWriter','flush','()',['java.io.IOException']]\r\n" + 
			",['java.io.FileReader','<init>','(java.io.File)',['java.io.FileNotFoundException']]\r\n" + 
			",['java.io.InputStreamReader','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.FileWriter','<init>','(java.io.File)',['java.io.IOException']]\r\n" + 
			",['java.io.FileWriter','<init>','(java.io.File,boolean)',['java.io.IOException']]\r\n" + 
			",['java.io.OutputStreamWriter','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.OutputStreamWriter','flush','()',['java.io.IOException']]\r\n" + 
			",['java.lang.reflect.Constructor','newInstance','(java.lang.Object[])',['java.lang.IllegalAccessException','java.lang.IllegalArgumentException','java.lang.InstantiationException','java.lang.reflect.InvocationTargetException']]\r\n" + 
			",['java.net.Socket','close','()',['java.io.IOException']]\r\n" + 
			",['java.net.Socket','getOutputStream','()',['java.io.IOException']]\r\n" + 
			",['java.net.Socket','getInputStream','()',['java.io.IOException']]\r\n" + 
			",['java.net.Socket','<init>','(java.net.InetAddress,int)',['java.io.IOException']]\r\n" + 
			",['java.util.logging.FileHandler','<init>','(java.lang.String)',['java.io.IOException','java.lang.SecurityException']]\r\n" + 
			",['java.util.logging.Handler','setFormatter','(java.util.logging.Formatter)',['java.lang.SecurityException']]\r\n" + 
			",['java.util.logging.Logger','setLevel','(java.util.logging.Level)',['java.lang.SecurityException']]\r\n" + 
			",['java.util.logging.Logger','addHandler','(java.util.logging.Handler)',['java.lang.SecurityException']]\r\n" + 
			",['javax.imageio.ImageIO','write','(java.awt.image.RenderedImage,java.lang.String,java.io.File)',['java.io.IOException']]\r\n" + 
			",['javax.swing.JOptionPane','showConfirmDialog','(java.awt.Component,java.lang.Object)',['java.awt.HeadlessException']]\r\n" + 
			",['javax.swing.SwingUtilities','invokeAndWait','(java.lang.Runnable)',['java.lang.InterruptedException','java.lang.reflect.InvocationTargetException']]\r\n" + 
			",['ognl.Ognl','getValue','(java.lang.Object,java.util.Map,java.lang.Object)',['ognl.OgnlException']]\r\n" + 
			",['ognl.Ognl','parseExpression','(java.lang.String)',['ognl.OgnlException']]\r\n" + 
			",['org.picocontainer.defaults.DefaultPicoContainer','registerComponentInstance','(java.lang.Object)',['org.picocontainer.PicoRegistrationException']]\r\n" + 
			",['org.picocontainer.defaults.DefaultPicoContainer','registerComponentImplementation','(java.lang.Class)',['org.picocontainer.PicoRegistrationException']]\r\n" + 
			",['java.lang.Class','getFields','()',['java.lang.SecurityException']]\r\n" + 
			",['com.nfe.guardian.shared.model.response.NFEGuardianResponse','toXML','(java.io.Writer)',['java.lang.Exception']]\r\n" + 
			",['com.nfe.guardian.shared.model.response.NFEGuardianResponse','toXML','()',['java.lang.Exception']]\r\n" + 
			",['java.beans.Introspector','getBeanInfo','(java.lang.Class)',['java.beans.IntrospectionException']]\r\n" + 
			",['java.io.InputStream','available','()',['java.io.IOException']]\r\n" + 
			",['java.io.InputStream','reset','()',['java.io.IOException']]\r\n" + 
			",['java.lang.String','getBytes','(java.lang.String)',['java.io.UnsupportedEncodingException']]\r\n" + 
			",['javax.xml.bind.JAXBContext','createUnmarshaller','()',['javax.xml.bind.JAXBException']]\r\n" + 
			",['javax.xml.bind.JAXBContext','newInstance','(java.lang.Class[])',['javax.xml.bind.JAXBException']]\r\n" + 
			",['javax.xml.bind.JAXBContext','createMarshaller','()',['javax.xml.bind.JAXBException']]\r\n" + 
			",['javax.xml.bind.JAXBContext','newInstance','(java.lang.String)',['javax.xml.bind.JAXBException']]\r\n" + 
			",['javax.xml.bind.Marshaller','marshal','(java.lang.Object,org.w3c.dom.Node)',['javax.xml.bind.JAXBException']]\r\n" + 
			",['javax.xml.bind.Marshaller','setProperty','(java.lang.String,java.lang.Object)',['javax.xml.bind.PropertyException']]\r\n" + 
			",['javax.xml.bind.Marshaller','marshal','(java.lang.Object,java.io.Writer)',['javax.xml.bind.JAXBException']]\r\n" + 
			",['javax.xml.bind.Unmarshaller','unmarshal','(java.io.InputStream)',['javax.xml.bind.JAXBException']]\r\n" + 
			",['javax.xml.transform.Transformer','transform','(javax.xml.transform.Source,javax.xml.transform.Result)',['javax.xml.transform.TransformerException']]\r\n" + 
			",['javax.xml.transform.Transformer','setOutputProperty','(java.lang.String,java.lang.String)',['java.lang.IllegalArgumentException']]\r\n" + 
			",['javax.xml.transform.TransformerFactory','newInstance','()',['javax.xml.transform.TransformerFactoryConfigurationError']]\r\n" + 
			",['javax.xml.transform.TransformerFactory','newTransformer','()',['javax.xml.transform.TransformerConfigurationException']]\r\n" + 
			",['org.w3c.dom.Element','removeAttribute','(java.lang.String)',['org.w3c.dom.DOMException']]\r\n" + 
			",['org.w3c.dom.Node','getTextContent','()',['org.w3c.dom.DOMException']]\r\n" + 
			",['java.io.FilterInputStream','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.InputStream','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.BufferedWriter','newLine','()',['java.io.IOException']]\r\n" + 
			",['java.io.DataInputStream','readUTF','()',['java.io.IOException']]\r\n" + 
			",['java.io.DataInputStream','readDouble','()',['java.io.IOException']]\r\n" + 
			",['java.io.DataInputStream','readInt','()',['java.io.IOException']]\r\n" + 
			",['java.io.DataOutputStream','flush','()',['java.io.IOException']]\r\n" + 
			",['java.io.DataOutputStream','writeUTF','(java.lang.String)',['java.io.IOException']]\r\n" + 
			",['java.io.DataOutputStream','writeDouble','(double)',['java.io.IOException']]\r\n" + 
			",['java.io.DataOutputStream','writeInt','(int)',['java.io.IOException']]\r\n" + 
			",['java.io.FilterOutputStream','close','()',['java.io.IOException']]\r\n" + 
			",['java.io.File','createTempFile','(java.lang.String,java.lang.String)',['java.io.IOException']]\r\n" + 
			",['java.io.FileOutputStream','<init>','(java.lang.String)',['java.io.FileNotFoundException']]\r\n" + 
			",['java.io.FileReader','<init>','(java.lang.String)',['java.io.FileNotFoundException']]\r\n" + 
			",['java.io.InputStreamReader','<init>','(java.io.InputStream,java.lang.String)',['java.io.UnsupportedEncodingException']]\r\n" + 
			",['java.io.OutputStreamWriter','<init>','(java.io.OutputStream,java.lang.String)',['java.io.UnsupportedEncodingException']]\r\n" + 
			",['java.io.ObjectInputStream','readUTF','()',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectInputStream','readDouble','()',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectInputStream','readInt','()',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectOutputStream','flush','()',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectOutputStream','writeUTF','(java.lang.String)',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectOutputStream','writeDouble','(double)',['java.io.IOException']]\r\n" + 
			",['java.io.ObjectOutputStream','writeInt','(int)',['java.io.IOException']]\r\n" + 
			",['java.lang.Double','parseDouble','(java.lang.String)',['java.lang.NumberFormatException']]\r\n" + 
			",['java.lang.Float','parseFloat','(java.lang.String)',['java.lang.NumberFormatException']]\r\n" + 
			",['java.security.MessageDigest','getInstance','(java.lang.String)',['java.security.NoSuchAlgorithmException']]\r\n" + 
			",['java.util.concurrent.Future','get','()',['java.lang.InterruptedException','java.util.concurrent.ExecutionException']]\r\n" + 
			",['java.util.zip.GZIPInputStream','<init>','(java.io.InputStream)',['java.io.IOException']]\r\n" + 
			",['java.util.zip.GZIPOutputStream','<init>','(java.io.OutputStream)',['java.io.IOException']]\r\n" + 
			",['org.apache.commons.logging.LogFactory','getLog','(java.lang.Class)',['org.apache.commons.logging.LogConfigurationException']]\r\n" + 
			",['com.sun.akuma.Daemon','init','(java.lang.String)',['java.lang.Exception']]\r\n" + 
			",['com.sun.akuma.Daemon','daemonize','()',['java.io.IOException']]\r\n" + 
			",['java.lang.String','<init>','(byte[],int,int,java.lang.String)',['java.io.UnsupportedEncodingException']]\r\n" + 
			",['java.io.Reader','read','(char[],int,int)',['java.io.IOException']]\r\n" + 
			",['java.net.URI','<init>','(java.lang.String)',['java.net.URISyntaxException']]\r\n" + 
			",['java.net.URL','openStream','()',['java.io.IOException']]\r\n" + 
			",['java.io.FileInputStream','close','()',['java.io.IOException']]\r\n" + 
			",['java.net.InetAddress','getByAddress','(byte[])',['java.net.UnknownHostException']]\r\n" + 
			",['java.util.Properties','load','(java.io.InputStream)',['java.io.IOException']]\r\n" + 
			",['java.security.KeyFactory','getInstance','(java.lang.String)',['java.security.NoSuchAlgorithmException']]\r\n" + 
			",['java.security.KeyFactory','generatePrivate','(java.security.spec.KeySpec)',['java.security.spec.InvalidKeySpecException']]\r\n" + 
			",['java.security.KeyStore','getInstance','(java.lang.String)',['java.security.KeyStoreException']]\r\n" + 
			",['java.security.KeyStore','load','(java.io.InputStream,char[])',['java.io.IOException','java.security.NoSuchAlgorithmException','java.security.cert.CertificateException']]\r\n" + 
			",['java.security.cert.CertificateFactory','getInstance','(java.lang.String)',['java.security.cert.CertificateException']]\r\n" + 
			",['java.security.cert.CertificateFactory','generateCertificate','(java.io.InputStream)',['java.security.cert.CertificateException']]\r\n" + 
			",['javax.net.ssl.KeyManagerFactory','init','(java.security.KeyStore,char[])',['java.security.KeyStoreException','java.security.NoSuchAlgorithmException','java.security.UnrecoverableKeyException']]\r\n" + 
			",['javax.net.ssl.KeyManagerFactory','getInstance','(java.lang.String)',['java.security.NoSuchAlgorithmException']]\r\n" + 
			",['javax.net.ssl.SSLContext','init','(javax.net.ssl.KeyManager[],javax.net.ssl.TrustManager[],java.security.SecureRandom)',['java.security.KeyManagementException']]\r\n" + 
			",['javax.net.ssl.SSLContext','getInstance','(java.lang.String)',['java.security.NoSuchAlgorithmException']]\r\n" + 
			",['javax.net.ssl.SSLSession','getPeerCertificateChain','()',['javax.net.ssl.SSLPeerUnverifiedException']]\r\n" + 
			",['javax.security.cert.Certificate','getEncoded','()',['javax.security.cert.CertificateEncodingException']]\r\n" + 
			",['net.oauth.OAuthAccessor','newRequestMessage','(java.lang.String,java.lang.String,java.util.Collection)',['java.io.IOException','java.net.URISyntaxException','net.oauth.OAuthException']]\r\n" + 
			",['net.oauth.OAuthMessage','getAuthorizationHeader','(java.lang.String)',['java.io.IOException']]\r\n" + 
			",['net.oauth.signature.pem.PEMReader','<init>','(java.io.InputStream)',['java.io.IOException']]\r\n" + 
			",['net.oauth.signature.pem.PKCS1EncodedKeySpec','<init>','(byte[])',['java.io.IOException']]\r\n" + 
			",['org.apache.log4j.FileAppender','<init>','(org.apache.log4j.Layout,java.lang.String)',['java.io.IOException']]\r\n" + 
			",['org.jboss.netty.channel.SimpleChannelUpstreamHandler','handleUpstream','(org.jboss.netty.channel.ChannelHandlerContext,org.jboss.netty.channel.ChannelEvent)',['java.lang.Exception']]\r\n" + 
			",['org.jboss.netty.channel.SimpleChannelHandler','handleUpstream','(org.jboss.netty.channel.ChannelHandlerContext,org.jboss.netty.channel.ChannelEvent)',['java.lang.Exception']]\r\n" + 
			",['org.jboss.netty.channel.SimpleChannelHandler','handleDownstream','(org.jboss.netty.channel.ChannelHandlerContext,org.jboss.netty.channel.ChannelEvent)',['java.lang.Exception']]\r\n" + 
			"]\r\n" 
			;

	public ERR54_OLD() {
		super(false);
	}

	private static final String CREATE_ALL_SUBTYPE_RELS = "MATCH (n)-[:HAS_CLASS_EXTENDS | :HAS_CLASS_IMPLEMENTS]->()-[:PARAMETERIZEDTYPE_TYPE*0..]->(m), (mType) WHERE SPLIT(mType.fullyQualifiedName,'<')[0]=SPLIT(m.actualType,'<')[0] MERGE (n)-[r:IS_SUBTYPE_OF]->(mType) ON CREATE SET r.created=TRUE "
			+ "WITH DISTINCT ['java.lang.AutoCloseable','java.io.BufferedReader','java.io.BufferedWriter' ] as autocloseableTypeNames, sucessors ";

	// public static void main(String[] args) {
	// System.out.println(new ERR54().queryToString());
	// }
	@Override
	protected void initiate() {

		clauses = new Clause[] { (Clause) getCFGServices().getCFGSuccesorsOf(new NodeVar("newStat"), ""),
				new SimpleWithClause("DISTINCT COLLECT([newStat, succesors])  as sucessors"),
				new ClauseImpl(CREATE_ALL_SUBTYPE_RELS),
				// new ClauseImpl("MATCH (n)-[r:HAS_VARIABLEDECL_INIT]->(m)
				// CREATE (n)-[:
				new MatchClause(true, TypeServicesWiggle.getSuperTypesOf(new NodeVar("closeableSubtype"))),
				new WhereClause("superType.fullyQualifiedName IN autocloseableTypeNames"), new SimpleWithClause(
						"DISTINCT sucessors,autocloseableTypeNames+COLLECT(closeableSubtype.fullyQualifiedName) as autocloseableTypeNames"),

				new MatchClause(true,
						"  (subtype)-[:IS_SUBTYPE_OF*0..]->(enclClass)-[:DECLARES_METHOD]->(m)-[:HAS_METHODDECL_THROWS]->()"),
				new MatchClause(true, "  (m)-[:HAS_METHODDECL_PARAMETERS]->(p)"), new SimpleWithClause(
						"DISTINCT sucessors,autocloseableTypeNames, CASE WHEN subtype.fullyQualifiedName CONTAINS '<' THEN SPLIT(subtype.fullyQualifiedName,'<')[0] ELSE subtype.fullyQualifiedName END as enclClass, m,p ORDER BY ID(p)"),

				new SimpleWithClause("autocloseableTypeNames,sucessors, "
						+ "enclClass, m.name as methodName, REDUCE(s='(', p IN COLLECT(p) | s+p.actualType+',')  as paramTypes"),
				new SimpleWithClause("DISTINCT autocloseableTypeNames,sucessors, "
						+ "COLLECT([enclClass, methodName, CASE WHEN paramTypes CONTAINS ',' THEN SUBSTRING(paramTypes, 0,LENGTH(paramTypes)-1) ELSE paramTypes END +')'])+"
						+ METHOD_THAT_THROWS + " as mThrowsInfo"),
				new ClauseImpl(
						"MATCH(inv)<-[:ARRAYACCESS_EXPR|ARRAYACCESS_INDEX|ASSIGNMENT_LHS|ASSIGNMENT_RHS|BINOP_LHS|BINOP_RHS|CAST_ENCLOSES|COMPOUND_ASSIGNMENT_LHS|COMPOUND_ASSIGNMENT_RHS|CONDITIONAL_CONDITION|CONDITIONAL_THEN|CONDITIONAL_ELSE|HAS_VARIABLEDECL_INIT|INSTANCEOF_EXPR|PARENTHESIZED_ENCLOSES|MEMBER_SELECT_EXPR|METHODINVOCATION_ARGUMENTS|METHODINVOCATION_METHOD_SELECT|NEW_CLASS_ARGUMENTS|NEWARRAY_INIT|NEWARRAY_DIMENSION|UNARY_ENCLOSES*0..]-()<-[:ASSERT_CONDITION|DOWHILE_CONDITION|EXPR_ENCLOSES|FOREACH_EXPR|FORLOOP_CONDITION|HAS_VARIABLEDECL_INIT|IF_CONDITION|RETURN_EXPR|SWITCH_EXPR|SYNCHRONIZED_EXPR|THROW_EXPR|WHILE_CONDITION]-(stat)<-[:CASE_STATEMENTS|CATCH_BLOCK|CATCH_PARAM|ENCLOSES|FOREACH_STATEMENT|FOREACH_VAR|FORLOOP_INIT|FORLOOP_STATEMENT|FORLOOP_UPDATE|HAS_METHODDECL_PARAMETERS|IF_THEN|IF_ELSE|LABELED_STATEMENT|SWITCH_ENCLOSES_CASES|SYNCHRONIZED_BLOCK|TRY_BLOCK|TRY_CATCH|TRY_FINALLY|TRY_RESOURCES*]-()<-[:HAS_METHODDECL_BODY]-({nodeType:'JCMethodDecl'})<-[:DECLARES_METHOD]-(callerEncType)\r\n"
								+ " WHERE inv.nodeType IN ['JCMethodInvocation', 'JCNewClass'] "
								+ "OPTIONAL MATCH (invMember)<-[:METHODINVOCATION_METHOD_SELECT]-(inv)"
								+ "OPTIONAL MATCH (invMember)-[:MEMBER_SELECT_EXPR]->(memberSelection)"
								+ "OPTIONAL MATCH (inv)-[:NEW_CLASS_ARGUMENTS]->(arg)"
								+ " WITH  autocloseableTypeNames,sucessors, mThrowsInfo,stat,inv,invMember,arg, CASE WHEN invMember IS NULL THEN inv.actualType ELSE CASE WHEN invMember.nodeType='JCIdent' THEN callerEncType.fullyQualifiedName ELSE memberSelection.actualType END END as callerEncTypeName ORDER BY ID(arg)"
								+ " WITH  DISTINCT " + "autocloseableTypeNames,sucessors, mThrowsInfo,"
								+ "stat,inv,invMember, callerEncTypeName, REDUCE(s='(', arg IN COLLECT(arg) | s+CASE WHEN s='(' THEN arg.actualType ELSE ','+arg.actualType END )+')' as newClassArgs "),

				new SimpleWithClause("DISTINCT " + "autocloseableTypeNames,sucessors,mThrowsInfo,"
						+ " stat, COLLECT([CASE WHEN callerEncTypeName CONTAINS '<' AND NOT callerEncTypeName STARTS WITH '<' THEN SPLIT(callerEncTypeName,'<')[0] ELSE callerEncTypeName END , CASE WHEN invMember IS NULL THEN '<init>' ELSE invMember.name END,CASE WHEN invMember IS NULL THEN newClassArgs ELSE SPLIT(invMember.actualType,')')[0]+')' END]) as invInfoInStats"),

				new WhereClause("ANY(invSignature IN invInfoInStats "
						+ " WHERE ANY(throwSignature IN mThrowsInfo WHERE throwSignature[0]=invSignature[0] AND throwSignature[1]=invSignature[1] AND throwSignature[2]=invSignature[2]) )"),
				new SimpleWithClause(
						"DISTINCT autocloseableTypeNames,sucessors, COLLECT( DISTINCT stat) as statsMayThrow"),
				new MatchClause(true,
						"(stat) WHERE stat.nodeType IN ['JCThrow', 'JCAssert'] WITH DISTINCT autocloseableTypeNames,sucessors, statsMayThrow+COLLECT(stat) as statsMayThrow"),

				new MatchClause(PDGServicesWiggle.WIGGLE.getIdsAndVarDeclarations(new NodeVar("id"),
						"autocloseableTypeNames,sucessors,statsMayThrow ")),
				new WhereClause("varDec.actualType IN autocloseableTypeNames"),
				new SimpleWithClause(
						"DISTINCT autocloseableTypeNames,sucessors,statsMayThrow, varDec as dec, COLLECT(DISTINCT id) as ids"),
				// new ReturnClause("DISTINCT dec, ids"),
				new MatchClause(true,
						"  (dec)<-[r:TRY_RESOURCES]-() WITH DISTINCT sucessors,statsMayThrow, dec,ids,r WHERE r IS NULL"),
				// new ReturnClause("DISTINCT dec")

				new MatchClause(true,
						getAssignmentServices().getLeftPartAssignments(new NodeVar("assignment"), new NodeVar("id")),
						getExpressionServices().getStatementFromExp(new NodeVar("assignment"))),
				new WhereClause("id IN ids"),
				new SimpleWithClause(" DISTINCT " + " sucessors,statsMayThrow,ids,  " + "dec, COLLECT(stat) as mods "),

				new MatchClause(true, new MatchImpl("(dec)-[r:HAS_VARIABLEDECL_INIT]->()")),
				new SimpleWithClause("DISTINCT " + " sucessors, statsMayThrow,"
						+ " dec, ids, mods + CASE WHEN r IS NULL THEN [] ELSE [dec] END as mods"),
				new UnwindClause("mods", "mod"),
				new MatchClause(MethodInvocationServicesWiggle.getMethodInvocationOf("close"),
						getExpressionServices().getStatementFromExp(new NodeVar("mInv"), new NodeVar("closeStat"))),

				new WhereClause("object IN ids"),
				new SimpleWithClause("DISTINCT sucessors,statsMayThrow, dec, mod, COLLECT(closeStat) as closeStmts"),
				new SimpleWithClause(
						" sucessors, dec, mod,closeStmts, FILTER( stat IN statsMayThrow WHERE NOT stat IN closeStmts AND stat IN FILTER(x IN sucessors WHERE x[0]=mod)[0][1]"
								+ " AND ANY(closeStat IN closeStmts WHERE closeStat IN FILTER(x IN sucessors WHERE x[0]=stat)[0][1]) ) as prevStats"),
				new UnwindClause("prevStats", "prevStat"),
				// new ReturnClause("DISTINCT FILTER(x IN sucessors WHERE
				// x[0].nodeType='JCCatch' )")

				new MatchClause(true,
						"p=" + getStatementServices().getOuterBlockFromStatement(new NodeVar("prevStat"),
								new NodeVar("try{nodeType:'JCTry'}")).matchToString()),
				new WhereClause("NOT TYPE(REVERSE(RELATIONSHIPS(p))[0]) IN ['TRY_CATCH', 'TRY_FINALLY']"),
				new MatchClause(true, "(try)-[:TRY_CATCH ]->(catch)"),
				new SimpleWithClause(
						"DISTINCT sucessors,dec, mod, prevStat,closeStmts, try, REDUCE(l=[],catch IN COLLECT(catch) | l+FILTER(x IN sucessors WHERE x[0]=catch)[0][1] ) as catchSucs"),
				new MatchClause(true,
						getStatementServices()
								.getOuterBlockFromStatement(new NodeVar("finallyStat"), new NodeVar("finally"))
								.matchToString() + "<-[:TRY_FINALLY]-(try)"),
				new SimpleWithClause(
						" DISTINCT dec, mod, prevStat,closeStmts, try, CASE WHEN SIZE(catchSucs)=0 THEN  COLLECT(finallyStat) ELSE catchSucs END as stmsReacheableAfterEx"),
				new SimpleWithClause(
						"DISTINCT dec, mod, prevStat,NOT ANY( reacheableStmt IN REDUCE(l=[], reacheables IN COLLECT(stmsReacheableAfterEx) | l+reacheables ) WHERE reacheableStmt IN closeStmts) as notReachebleClose"),
				new WhereClause(" notReachebleClose "),
				new MatchClause(getStatementServices().getEnclosingClassFromStatement(new NodeVar("dec"))),
				new SimpleWithClause(
						"DISTINCT 'Warning [CMU-ERR54] variable '+dec.name+ '(defined in line'+dec.lineNumber+', class '+enclClass.fullyQualifiedName+') might not be properly closed, as statement(s) (in lines '+ EXTRACT(prev IN COLLECT(DISTINCT prevStat) | prev.lineNumber)+') may throw an exception.' as warning")
				, new SimpleWithClause("DISTINCT COLLECT(warning) as warnings"), new MatchClause(false, "()-[r:IS_SUBTYPE_OF]->() DELETE r WITH DISTINCT warnings RETURN warnings")
				// , new WhereClause(" allReacheables ")
		};
	}

	public static void main(String[] args) {
		System.out.println(new ERR54_OLD().queryToString());
	}
}
