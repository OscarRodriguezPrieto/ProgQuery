package es.uniovi.reflection.progquery;

import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.EmbeddedInsertion;
import es.uniovi.reflection.progquery.database.Neo4jDriverLazyInsertion;
import es.uniovi.reflection.progquery.database.NotPersistentLazyInsertion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    //-user=progquery -program=ExampleClasses -neo4j_host=156.35.94.130 -neo4j_database=debug -neo4j_password=secreto
    // -src=C:\Users\VirtualUser\Source\Repos\StaticCodeAnalysis\Programs\ExampleClasses
    //-user=progquery -program=ExampleClasses -neo4j_database=debug -neo4j_mode=local
    // -src=C:\Users\Miguel\Source\codeanalysis\codeanalysis-tool\Programs\ExampleClasses

    public static Parameters parameters = new Parameters();

    public static void main(String[] args) {

        parseArguments(args);
        MultiCompilationScheduler scheduler =
                startInsertion(parameters.neo4j_mode, parameters.neo4j_host, parameters.neo4j_port_number,
                        parameters.neo4j_user, parameters.neo4j_password, parameters.neo4j_database,
                        parameters.max_operations_transaction, parameters.programId, parameters.userId,
                        parameters.sourceFolder);

        final String JAVAC_DEFAULT_VERSION = "15";
        scheduler.newCompilationTask(parameters.sourceFolder, parameters.class_path, JAVAC_DEFAULT_VERSION,
                JAVAC_DEFAULT_VERSION);

        scheduler.endAnalysis();

        System.exit(0);
    }

    public static MultiCompilationScheduler startInsertion(String neo4jMode, String neo4jHost, String neo4jPort,
                                                           String neo4jUser, String neo4jPass, String database,
                                                           String maxOps, String programId, String userId,
                                                           String dbFolder) {

        try {
            DatabaseFachade.init(neo4jMode.contentEquals(OptionsConfiguration.NEO4J_MODE_SERVER) ?
                    new Neo4jDriverLazyInsertion(neo4jHost, neo4jPort, neo4jUser, neo4jPass, database, maxOps) :
                    neo4jMode.contentEquals(OptionsConfiguration.NEO4J_MODE_LOCAL) ? new EmbeddedInsertion(
                            Paths.get(new File(dbFolder).getCanonicalPath(), "target", database).toAbsolutePath()
                                    .toString()) : new NotPersistentLazyInsertion());
        } catch (IOException e) {
            e.printStackTrace();
            final int ABNORMAL_TERMINATION = -1;
            System.exit(ABNORMAL_TERMINATION);
        }
        return new MultiCompilationScheduler(programId, userId);
    }

    public static MultiCompilationScheduler startServerInsertion(String neo4jHost, String neo4jPort, String neo4jUser,
                                                                 String neo4jPass, String database, String maxOps,
                                                                 String programId, String userId) {
        return startInsertion(OptionsConfiguration.NEO4J_MODE_SERVER, neo4jHost, neo4jPort, neo4jUser, neo4jPass,
                database, maxOps, programId, userId, null);
    }

    public static MultiCompilationScheduler startLocalInsertion(String database, String programId, String userId,
                                                                String dbFolder) {

        return startInsertion(OptionsConfiguration.NEO4J_MODE_LOCAL, null, null, null, null, database, null, programId, userId, dbFolder);
    }

    private static void parseArguments(String[] args) {
        setDefaultParameters();
        List<String> inputFileNames = new ArrayList<String>();
        for (String parameter : args) {
            parseParameter(parameter, inputFileNames);
        }

        if (parameters.userId.isEmpty()) {
            System.out.println(OptionsConfiguration.noUser);
            System.exit(0);
            return;
        }
        if (parameters.programId.isEmpty()) {
            System.out.println(OptionsConfiguration.noProgram);
            System.exit(0);
            return;
        }

        if (parameters.neo4j_mode == OptionsConfiguration.DEFAULT_NEO4J_MODE) { //server

            if (parameters.neo4j_host.isEmpty()) {
                System.out.println(OptionsConfiguration.noHost);
                System.exit(0);
                return;
            }
            if (parameters.neo4j_password.isEmpty()) {
                System.out.println(OptionsConfiguration.noPassword);
                System.exit(0);
                return;
            }
            if (parameters.neo4j_database.isEmpty()) {
                parameters.neo4j_database = parameters.userId;
            }
        }
        //else {
        //if (parameters.neo4j_database_path.isEmpty()) {
        //System.out.println(OptionsConfiguration.noDataBasePath);
        //System.exit(0);
        //return;

        // }
        //}
        if (parameters.sourceFolder.isEmpty()) {
            System.out.println(OptionsConfiguration.noInputMessage);
            System.exit(0);
            return;
        }
    }

    private static void setDefaultParameters() {
        parameters.neo4j_user = OptionsConfiguration.DEFAULT_NEO4J_USER;
        parameters.neo4j_port_number = OptionsConfiguration.DEFAULT_NEO4J_PORT;
        parameters.neo4j_mode = OptionsConfiguration.DEFAULT_NEO4J_MODE;
        parameters.max_operations_transaction = OptionsConfiguration.DEFAULT_MAX_OPERATIONS_TRANSACTION;
        parameters.verbose = OptionsConfiguration.DEFAULT_VERBOSE;
    }

    private static void parseParameter(String parameter, List<String> inputFiles) {
        for (String parameterPrefix : OptionsConfiguration.optionsPrefix) {
            if (parameter.startsWith(parameterPrefix)) {
                parseOption(parameter.substring(parameterPrefix.length(), parameter.length()).toLowerCase());
                return;
            }
        }
        inputFiles.add(parameter);
    }

    private static void parseOption(String option) {
        for (String opString : OptionsConfiguration.helpOptions) {
            if (option.equals(opString)) {
                System.out.println(OptionsConfiguration.helpMessage);
                System.exit(0);
            }
        }
        for (String opString : OptionsConfiguration.userOptions) {
            if (option.startsWith(opString)) {
                parameters.userId = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.programOptions) {
            if (option.startsWith(opString)) {
                parameters.programId = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.neo4j_databaseOptions) {
            if (option.startsWith(opString)) {
                parameters.neo4j_database = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.neo4j_database_pathOptions) {
            if (option.startsWith(opString)) {
                parameters.neo4j_database_path = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.neo4j_userOptions) {
            if (option.startsWith(opString)) {
                parameters.neo4j_user = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.neo4j_passwordOptions) {
            if (option.startsWith(opString)) {
                parameters.neo4j_password = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.sourceFolderOptions) {
            if (option.startsWith(opString)) {
                parameters.sourceFolder = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.neo4j_hostOptions) {
            if (option.startsWith(opString)) {
                parameters.neo4j_host = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.neo4j_port_numberOptions) {
            if (option.startsWith(opString)) {
                parameters.neo4j_port_number = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.neo4j_modeOptions) {
            if (option.startsWith(opString)) {
                String modeOption = parseValue(option.substring(opString.length(), option.length()));
                if (Arrays.asList(OptionsConfiguration.neo4j_modeNames).indexOf(modeOption) == -1) {
                    System.err.println(OptionsConfiguration.unknownNEO4JMode);
                    System.exit(1);
                }
                parameters.neo4j_mode = modeOption;
                return;
            }
        }
        for (String opString : OptionsConfiguration.max_operations_transactionOptions) {
            if (option.startsWith(opString)) {
                parameters.max_operations_transaction =
                        parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.classPathOptions) {
            if (option.startsWith(opString)) {
                parameters.class_path = parseValue(option.substring(opString.length(), option.length()));
                return;
            }
        }
        for (String opString : OptionsConfiguration.verboseOptions) {
            if (option.startsWith(opString)) {
                parameters.verbose = true;
                return;
            }
        }
        System.err.println(OptionsConfiguration.errorMessage);
        System.exit(1);  // 1 == Unknown option
    }

    private static String parseValue(String value) {
        for (String opAssignment : OptionsConfiguration.optionsAssignment)
            if (value.startsWith(opAssignment))
                return value.substring(opAssignment.length(), value.length());
        System.err.println(OptionsConfiguration.errorMessage);
        System.exit(2);  // 2 == Bad option assignment
        return null;
    }


}
