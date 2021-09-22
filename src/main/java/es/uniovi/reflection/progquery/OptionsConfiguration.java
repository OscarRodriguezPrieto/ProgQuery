package es.uniovi.reflection.progquery;

public class OptionsConfiguration {
	private static final String copyrightMessage =  "ProgQuery 2.0 - Computational Reflection Research Group (University of Oviedo)\n";;	
	public static final String helpMessage = copyrightMessage + "\nOptions:\n" +
		"\t-help\n\t\tDisplays this usage message (Short form: -?).\n" +
		"\t-user=<user_id>\n\t\tUser id. (Short form:-u=<user_id>)\n" +
		"\t-program=<program_id>\n\t\tProgram identificator. (Short form:-p=<program_id>)\n" +
		"\t-neo4j_mode={local,server}\n\t\tNEO4J mode: local or server. (Default value is server, short form:-nm={local,server})\n" +
		"\t-neo4j_user=<user_name>\n\t\tNEO4J User name. (Default value is neo4j, short form:-nu=<user_name>)\n" +
		"\t-neo4j_password=<user_password>\n\t\tNEO4J User password. (Short form:-np=<user_pasword>)\n" +
		"\t-neo4j_host=<host>\n\t\tNEO4J Host address. (Short form:-nh=<host>)\n" +
		"\t-neo4j_port_number=<port_number>\n\t\tNEO4J Port number. (Default value is 7687, short form:-npn=<port_number>)\n" +
		"\t-neo4j_database=<database_name>\n\t\tNEO4J Database name. (Default value is the -user parameter value, short form:-ndb=<database_name>)\n" +
		"\t-neo4j_database_path=<database_path>\n\t\tNEO4J Database path, when Local mode is used. (Short form:-ndbp=<database_path>)\n" +
		"\t-max_operations_transaction=<number>\n\t\tMaximum number of operations per transaction. (Default value is 80000, short form:-mot=<number>)\n" +
		"\t-src=<source_folder>\n\t\tSource folder path that contains Java source code. (Short form:-s=<source_folder>)\n" +
		"\t-classpath:<java_classpath>\n\t\tJava classpath, location of user-defined classes and packages. (Short form:-cp=<java_classpath>)\n" +		
		"\t-verbose\n\t\tShows log info (Default value is false).\n" +		
		"\n";
	public static final String errorMessage = copyrightMessage + "\nSome error in the input parameters. Type -help for help.\n";
	public static final String noUser = copyrightMessage + "\nNo user specified. Type -help for help.\n";
	public static final String noProgram = copyrightMessage + "\nNo program specified. Type -help for help.\n";
	public static final String noHost = copyrightMessage + "\nNo NEO4J host specified. Type -help for help.\n";
	public static final String noPassword = copyrightMessage + "\nNo NEO4J user password specified. Type -help for help.\n";
	public static final String noDataBasePath = copyrightMessage + "\nNo es.uniovi.reflection.progquery.database path specified using NEO4J local mode. Type -help for help.\n";
	public static final String noInputMessage = copyrightMessage + "\nNo source folder specified. Type -help for help.\n";
	public static final String unknownNEO4JMode = copyrightMessage + "\nUnknown neo4j mode option. Type -help for help.\n";

	public static final String[] helpOptions = { "help" , "?" };	
	public static final String[] userOptions = { "user","u" };
	public static final String[] programOptions = { "program","p" };
	public static final String[] neo4j_databaseOptions = { "neo4j_database","ndb" };
	public static final String[] neo4j_database_pathOptions = { "neo4j_database_path","ndbp" };
	public static final String[] neo4j_userOptions = { "neo4j_user","nu" };
	public static final String[] neo4j_modeOptions = { "neo4j_mode","nm" };
	public static final String[] neo4j_modeNames = { "local", "server", "no" };
	public static final String[] neo4j_passwordOptions = { "neo4j_password","np" };
	public static final String[] neo4j_hostOptions = { "neo4j_host","nh" };
	public static final String[] neo4j_port_numberOptions = { "neo4j_port_number","npn" };
	public static final String[] max_operations_transactionOptions = { "max_operations_transaction","mot" };
	public static final String[] classPathOptions = { "classpath","cp" };
	public static final String[] sourceFolderOptions = { "src", "s" };
	public static final String[] verboseOptions = { "verbose" };

	public static final String[] optionsPrefix = { "-" };
	public static final String[] optionsAssignment = { "=" };  
	
	public static final boolean DEFAULT_VERBOSE = false;
	public static final String DEFAULT_NEO4J_MODE = "server";
	public static final String DEFAULT_NEO4J_PORT = "7687";
	public static final String DEFAULT_NEO4J_USER = "neo4j";
	public static final String DEFAULT_MAX_OPERATIONS_TRANSACTION = "80000";	
}
