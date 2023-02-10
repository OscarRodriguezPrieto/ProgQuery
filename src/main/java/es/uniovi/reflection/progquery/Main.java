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
        //        String[] classPaths = new String[]{
        //                "C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter" +
        //                        "-netflix-zuul\\2.2.8.RELEASE\\spring-cloud-starter-netflix-zuul-2.2.8.RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-zuul\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-zuul-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-hystrix\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-hystrix-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\netflix-commons\\netflix-commons-util\\0.3" +
        //                        ".0\\netflix-commons-util-0" +
        //                        ".3.0.jar;C:\\Users\\Oskar\\.m2\\repository\\javax\\inject\\javax.inject\\1\\javax
        //                        .inject-1" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-context\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-context-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-crypto\\5.3.9" +
        //                        ".RELEASE\\spring-security-crypto-5.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-commons\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-commons-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-rsa\\1.0.9" +
        //                        ".RELEASE\\spring-security-rsa-1.0.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\bouncycastle\\bcpkix-jdk15on\\1.64\\bcpkix-jdk15on-1.64.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\bouncycastle\\bcprov-jdk15on\\1.64\\bcprov-jdk15on-1.64.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-web\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-web-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-json\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-json-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-databind\\2.11
        //                        .4\\jackson-databind-2" +
        //                        ".11.4" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\fasterxml\\jackson\\datatype\\jackson-datatype" +
        //                        "-jdk8\\2" + ".11.4\\jackson-datatype-jdk8-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\datatype\\jackson-datatype-jsr310\\2.11" +
        //                        ".4\\jackson-datatype-jsr310-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\module\\jackson-module-parameter-names
        //                        \\2.11" +
        //                        ".4\\jackson-module-parameter-names-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-tomcat\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-tomcat-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\tomcat\\embed\\tomcat-embed-core\\9.0
        //                        .46\\tomcat-embed-core-9" +
        //                        ".0.46" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\glassfish\\jakarta.el\\3.0.3\\jakarta
        //                        .el-3.0.3" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\apache\\tomcat\\embed\\tomcat-embed-websocket\\9.0" +
        //                        ".46\\tomcat-embed-websocket-9.0.46.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-web\\5.2.15.RELEASE\\spring-web-5.2
        //                        .15.RELEASE" +
        //                        ".jar;" + "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\spring-webmvc\\5.2.15" +
        //                        ".RELEASE\\spring-webmvc-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-actuator\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-actuator-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-actuator-autoconfigure\\2
        //                        .3.12" +
        //                        ".RELEASE\\spring-boot-actuator-autoconfigure-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-actuator\\2.3.12" +
        //                        ".RELEASE\\spring-boot-actuator-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\io\\micrometer\\micrometer-core\\1.5.14\\micrometer-core-1.5.14
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\hdrhistogram\\HdrHistogram\\2.1
        //                        .12\\HdrHistogram-2.1" +
        //                        ".12.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\latencyutils\\LatencyUtils\\2.0
        //                        .3\\LatencyUtils-2.0" +
        //                        ".3.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix" +
        //                        "-hystrix" +
        //                        "\\2.2.8.RELEASE\\spring-cloud-starter-netflix-hystrix-2.2.8.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-ribbon\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-ribbon-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix-core\\1.5.18\\hystrix-core-1.5.18
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\hystrix\\hystrix-serialization\\1
        //                        .5" +
        //                        ".18\\hystrix-serialization-1.5.18.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\module\\jackson-module-afterburner\\2.11" +
        //                        ".4\\jackson-module-afterburner-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-core\\2.11
        //                        .4\\jackson-core-2.11.4" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-annotations\\2.11" +
        //                        ".4\\jackson-annotations-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix-metrics-event-stream\\1.5" +
        //                        ".18\\hystrix-metrics-event-stream-1.5.18.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix-javanica\\1.5
        //                        .18\\hystrix-javanica-1.5.18" +
        //                        ".jar;" + "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\apache\\commons\\commons-lang3\\3" +
        //                        ".10\\commons-lang3-3.10" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\ow2\\asm\\asm\\5.0.4\\asm-5.0.4.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\guava\\guava\\30.0-jre\\guava-30.0-jre.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\guava\\failureaccess\\1.0.1\\failureaccess-1.0.1
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\google\\guava\\listenablefuture\\9999" +
        //                        ".0-empty-to-avoid-conflict-with-guava\\listenablefuture-9999" +
        //                        ".0-empty-to-avoid-conflict-with-guava" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\checkerframework\\checker-qual\\3.5" +
        //                        ".0\\checker-qual-3.5" +
        //                        ".0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\google\\errorprone\\error_prone_annotations" +
        //                        "\\2.3" + ".4\\error_prone_annotations-2.3.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\j2objc\\j2objc-annotations\\1
        //                        .3\\j2objc-annotations-1.3.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxjava-reactive-streams\\1.2" +
        //                        ".1\\rxjava-reactive-streams-1.2.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix-ribbon
        //                        \\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-netflix-ribbon-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\ribbon\\ribbon\\2.3.0\\ribbon-2.3.0.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\ribbon\\ribbon-transport\\2.3.0\\ribbon-transport-2
        //                        .3.0.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxnetty-contexts\\0.4" +
        //                        ".9\\rxnetty-contexts-0.4.9" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxnetty-servo\\0.4
        //                        .9\\rxnetty-servo-0" +
        //                        ".4.9" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxnetty\\0.4.9\\rxnetty-0.4
        //                        .9.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\ribbon\\ribbon-core\\2.3
        //                        .0\\ribbon-core-2.3" +
        //                        ".0.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\commons-lang\\commons-lang\\2.6\\commons-lang-2
        //                        .6.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\ribbon\\ribbon-httpclient\\2.3" +
        //                        ".0\\ribbon-httpclient-2.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons-collections\\commons-collections\\3.2
        //                        .2\\commons-collections-3.2.2" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\sun\\jersey\\jersey-client\\1.19
        //                        .1\\jersey-client-1" +
        //                        ".19.1" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\com\\sun\\jersey\\jersey-core\\1.19
        //                        .1\\jersey-core-1" +
        //                        ".19.1" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\javax\\ws\\rs\\jsr311-api\\1.1
        //                        .1\\jsr311-api-1.1.1" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\sun\\jersey\\contribs\\jersey-apache-client4\\1.19" +
        //                        ".1\\jersey-apache-client4-1.19.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\ribbon\\ribbon-loadbalancer\\2.3
        //                        .0\\ribbon-loadbalancer-2.3.0" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\netflix\\netflix-commons\\netflix-statistics\\0.1" +
        //                        ".1\\netflix-statistics-0.1.1.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\io\\reactivex\\rxjava\\1" +
        //                        ".3" + ".8\\rxjava-1.3.8.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix
        //                        -archaius\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-netflix-archaius-2.2.8.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-archaius\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-archaius-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\archaius\\archaius-core\\0.7.7\\archaius-core-0.7.7
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\google\\code\\findbugs\\jsr305\\3.0
        //                        .1\\jsr305-3.0.1" +
        //                        ".jar;" + "C:\\Users\\Oskar\\
        //                        .m2\\repository\\commons-configuration\\commons-configuration\\1" +
        //                        ".8\\commons-configuration-1.8.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\zuul\\zuul-core\\1.3.1\\zuul-core-1.3.1.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons-io\\commons-io\\2.4\\commons-io-2.4.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\slf4j\\slf4j-api\\1.7.30\\slf4j-api-1.7.30.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\servo\\servo-core\\0.12.21\\servo-core-0.12.21.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-autoconfigure-processor
        //                        \\2.3.12" +
        //                        ".RELEASE\\spring-boot-autoconfigure-processor-2.3.12.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-data-redis\\2.3
        //                        .12" +
        //                        ".RELEASE\\spring-boot-starter-data-redis-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot\\2.3.12
        //                        .RELEASE\\spring-boot-2.3.12" +
        //                        ".RELEASE" + ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot" +
        //                        "-autoconfigure\\2.3" +
        //                        ".12.RELEASE\\spring-boot-autoconfigure-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-logging\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-logging-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\ch\\qos\\logback\\logback-classic\\1.2.3\\logback-classic-1.2.3
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\ch\\qos\\logback\\logback-core\\1.2
        //                        .3\\logback-core-1.2.3" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\logging\\log4j\\log4j-to-slf4j\\2
        //                        .13" +
        //                        ".3\\log4j-to-slf4j-2.13.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\logging\\log4j\\log4j-api\\2.13.3\\log4j-api-2.13.3
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\slf4j\\jul-to-slf4j\\1.7
        //                        .30\\jul-to-slf4j-1.7.30" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\jakarta\\annotation\\jakarta.annotation-api\\1
        //                        .3" +
        //                        ".5\\jakarta" +
        //                        ".annotation-api-1.3.5.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\yaml\\snakeyaml\\1" +
        //                        ".26\\snakeyaml-1" +
        //                        ".26.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\data\\spring-data-redis\\2" +
        //                        ".3.9" + ".RELEASE\\spring-data-redis-2.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\data\\spring-data-keyvalue\\2.3.9" +
        //                        ".RELEASE\\spring-data-keyvalue-2.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-tx\\5.2.15.RELEASE\\spring-tx-5.2.15
        //                        .RELEASE" +
        //                        ".jar;" + "C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-oxm\\5.2
        //                        .15" +
        //                        ".RELEASE\\spring-oxm-5.2" +
        //                        ".15.RELEASE.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\spring-context" +
        //                        "-support\\5.2" + ".15.RELEASE\\spring-context-support-5.2.15.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\io\\lettuce\\lettuce-core\\5.3.7.RELEASE\\lettuce-core-5.3.7
        //                        .RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\io\\netty\\netty-common\\4.1.65
        //                        .Final\\netty-common-4.1" +
        //                        ".65.Final" + ".jar;C:\\Users\\Oskar\\.m2\\repository\\io\\netty\\netty-handler\\4
        //                        .1.65" +
        //                        ".Final\\netty-handler-4.1.65" +
        //                        ".Final.jar;C:\\Users\\Oskar\\.m2\\repository\\io\\netty\\netty-resolver\\4.1.65" +
        //                        ".Final\\netty-resolver-4.1.65.Final.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\io\\netty\\netty-buffer\\4.1.65.Final\\netty-buffer-4.1.65.Final
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\io\\netty\\netty-codec\\4.1.65
        //                        .Final\\netty-codec-4.1.65" +
        //                        ".Final" + ".jar;C:\\Users\\Oskar\\.m2\\repository\\io\\netty\\netty-transport\\4.1
        //                        .65" +
        //                        ".Final\\netty-transport-4" +
        //                        ".1.65.Final.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\io\\projectreactor\\reactor-core\\3.3.17" +
        //                        ".RELEASE\\reactor-core-3.3.17.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-consul\\2.2.7" +
        //                        ".RELEASE\\spring-cloud-starter-consul-2.2.7.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-consul-core\\2.2.7" +
        //                        ".RELEASE\\spring-cloud-consul-core-2.2.7.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-validation\\2.3
        //                        .12" +
        //                        ".RELEASE\\spring-boot-starter-validation-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\hibernate\\validator\\hibernate-validator\\6.1.7" +
        //                        ".Final\\hibernate-validator-6" +
        //                        ".1.7.Final.jar;C:\\Users\\Oskar\\.m2\\repository\\jakarta\\validation\\jakarta" +
        //                        ".validation-api\\2.0" + ".2\\jakarta.validation-api-2.0.2.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\ecwid\\consul\\consul-api\\1.4.5\\consul-api-1.4.5.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\code\\gson\\gson\\2.8.7\\gson-2.8.7.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\httpcomponents\\httpclient\\4.5.13\\httpclient-4.5
        //                        .13.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\commons-codec\\commons-codec\\1
        //                        .14\\commons-codec-1.14" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\httpcomponents\\httpcore\\4.4
        //                        .14\\httpcore-4" +
        //                        ".4.14" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot-starter-data" +
        //                        "-jpa\\2" +
        //                        ".3.12.RELEASE\\spring-boot-starter-data-jpa-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-aop\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-aop-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\aspectj\\aspectjweaver\\1.9.6\\aspectjweaver-1.9.6.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-jdbc\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-jdbc-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\zaxxer\\HikariCP\\3.4.5\\HikariCP-3.4.5.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-jdbc\\5.2.15.RELEASE\\spring-jdbc-5
        //                        .2.15" +
        //                        ".RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\jakarta\\transaction\\jakarta
        //                        .transaction-api\\1.3" +
        //                        ".3\\jakarta" +
        //                        ".transaction-api-1.3.3.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\jakarta\\persistence\\jakarta" +
        //                        ".persistence-api\\2.2.3\\jakarta.persistence-api-2.2.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\hibernate\\hibernate-core\\5.4.32.Final\\hibernate-core-5.4
        //                        .32.Final" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\javassist\\javassist\\3.27
        //                        .0-GA\\javassist-3.27.0-GA" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\net\\bytebuddy\\byte-buddy\\1.10
        //                        .22\\byte-buddy-1.10.22" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\antlr\\antlr\\2.7.7\\antlr-2.7.7.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\jboss\\jandex\\2.2.3.Final\\jandex-2.2.3.Final.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\classmate\\1.5.1\\classmate-1.5.1.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\dom4j\\dom4j\\2.1.3\\dom4j-2.1.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\hibernate\\common\\hibernate-commons-annotations\\5.1.2" +
        //                        ".Final\\hibernate-commons-annotations-5.1.2.Final.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\glassfish\\jaxb\\jaxb-runtime\\2.3.4\\jaxb-runtime-2.3.4
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\glassfish\\jaxb\\txw2\\2.3.4\\txw2-2.3.4
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\sun\\istack\\istack-commons-runtime\\3.0" +
        //                        ".12\\istack-commons-runtime-3.0.12.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\sun\\activation\\jakarta.activation\\1.2.2\\jakarta
        //                        .activation-1.2.2" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\data\\spring-data-jpa\\2
        //                        .3.9" +
        //                        ".RELEASE\\spring-data-jpa-2.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\data\\spring-data-commons\\2.3.9" +
        //                        ".RELEASE\\spring-data-commons-2.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-orm\\5.2.15.RELEASE\\spring-orm-5.2
        //                        .15.RELEASE" +
        //                        ".jar;" + "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\spring-context\\5.2.15" +
        //                        ".RELEASE\\spring-context-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-beans\\5.2.15
        //                        .RELEASE\\spring-beans-5.2.15" +
        //                        ".RELEASE" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-aspects\\5.2
        //                        .15" +
        //                        ".RELEASE\\spring-aspects-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-security\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-security-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-aop\\5.2.15.RELEASE\\spring-aop-5.2
        //                        .15.RELEASE" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\security\\spring-security-config\\5" +
        //                        ".3.9" + ".RELEASE\\spring-security-config-5.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-web\\5.3.9" +
        //                        ".RELEASE\\spring-security-web-5.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-expression\\5.2.15" +
        //                        ".RELEASE\\spring-expression-5.2.15" +
        //                        ".RELEASE.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j" +
        //                        "-core\\6" + ".3.0\\bucket4j-core-6.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-jcache\\6.3" +
        //                        ".0\\bucket4j-jcache-6.3.0" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j" +
        //                        "-hazelcast\\6.3" + ".0\\bucket4j-hazelcast-6.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-ignite\\6.3" +
        //                        ".0\\bucket4j-ignite-6.3.0" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j" +
        //                        "-infinispan\\6.3" + ".0\\bucket4j-infinispan-6.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\javax\\cache\\cache-api\\1.1" +
        //                        ".1\\cache-api-1.1.1.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\hazelcast\\hazelcast\\5" +
        //                        ".0\\hazelcast-5.0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\apache\\ignite\\ignite-core\\2" +
        //                        ".11" + ".0\\ignite-core-2.11.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\jetbrains\\annotations\\16.0" +
        //                        ".3\\annotations-16.0.3.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\gridgain\\ignite-shmem\\1" +
        //                        ".0" + ".0\\ignite-shmem-1.0.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\infinispan\\infinispan-core\\13.0" +
        //                        ".1.Final\\infinispan-core-13.0.1.Final.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\infinispan\\protostream\\protostream\\4.4.1
        //                        .Final\\protostream-4.4.1" +
        //                        ".Final" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\infinispan\\protostream\\protostream-types\\4.4" +
        //                        ".1" + ".Final\\protostream-types-4.4.1.Final.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\jgroups\\jgroups\\4.2.17.Final\\jgroups-4.2.17.Final.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\github\\ben-manes\\caffeine\\caffeine\\2.8.8\\caffeine-2.8.8
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\jboss\\logging\\jboss-logging\\3.4.2" +
        //                        ".Final\\jboss-logging-3" +
        //                        ".4.2.Final.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\jboss\\threads\\jboss-threads\\2.3.3" +
        //                        ".Final\\jboss-threads-2.3.3.Final.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\wildfly\\common\\wildfly-common\\1.3.0
        //                        .Final\\wildfly-common-1.3.0" +
        //                        ".Final.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\infinispan\\infinispan-commons\\13.0.1" +
        //                        ".Final\\infinispan-commons-13.0.1.Final.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\io\\reactivex\\rxjava3\\rxjava\\3.0.4\\rxjava-3.0.4.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\reactivestreams\\reactive-streams\\1.0.3\\reactive-streams-1
        //                        .0.3.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot-configuration" +
        //                        "-processor" +
        //                        "\\2.3.12.RELEASE\\spring-boot-configuration-processor-2.3.12.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-test\\5.3.9" +
        //                        ".RELEASE\\spring-security-test-5.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-core\\5.3.9" +
        //                        ".RELEASE\\spring-security-core-5.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-core\\5.2.15.RELEASE\\spring-core-5
        //                        .2.15" +
        //                        ".RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-jcl\\5.2.15" +
        //                        ".RELEASE\\spring-jcl-5.2" +
        //                        ".15.RELEASE.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\spring-test\\5.2.15" +
        //                        ".RELEASE\\spring-test-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons-net\\commons-net\\3.8.0\\commons-net-3.8.0.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-test\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-test-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-test\\2.3.12" +
        //                        ".RELEASE\\spring-boot-test-2.3" +
        //                        ".12.RELEASE.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot" +
        //                        "-test" + "-autoconfigure\\2.3.12.RELEASE\\spring-boot-test-autoconfigure-2.3.12
        //                        .RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\jayway\\jsonpath\\json-path\\2.4.0\\json-path-2.4.0.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\net\\minidev\\json-smart\\2.3.1\\json-smart-2.3.1.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\net\\minidev\\accessors-smart\\2.3.1\\accessors-smart-2.3.1.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\jakarta\\xml\\bind\\jakarta.xml.bind-api\\2.3.3\\jakarta.xml
        //                        .bind-api-2.3.3" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\jakarta\\activation\\jakarta.activation-api\\1
        //                        .2" +
        //                        ".2\\jakarta" +
        //                        ".activation-api-1.2.2.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\assertj\\assertj-core\\3" +
        //                        ".16" +
        //                        ".1\\assertj-core-3.16.1.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\hamcrest\\hamcrest\\2" +
        //                        ".2\\hamcrest-2.2.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\jupiter\\junit-jupiter\\5.6" +
        //                        ".3\\junit-jupiter-5.6.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\jupiter\\junit-jupiter-api\\5.6
        //                        .3\\junit-jupiter-api-5.6.3.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apiguardian\\apiguardian-api\\1.1" +
        //                        ".0\\apiguardian-api-1.1.0" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\opentest4j\\opentest4j\\1.2
        //                        .0\\opentest4j-1.2.0" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\platform\\junit-platform-commons\\1
        //                        .6" +
        //                        ".3\\junit-platform-commons-1.6.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\jupiter\\junit-jupiter-params\\5.6
        //                        .3\\junit-jupiter-params-5.6" +
        //                        ".3.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\jupiter\\junit-jupiter-engine\\5.6" +
        //                        ".3\\junit-jupiter-engine-5.6.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\platform\\junit-platform-engine\\1.6
        //                        .3\\junit-platform-engine-1" +
        //                        ".6.3" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\mockito\\mockito-core\\3.3
        //                        .3\\mockito-core-3.3" +
        //                        ".3.jar;" + "C:\\Users\\Oskar\\.m2\\repository\\net\\bytebuddy\\byte-buddy-agent\\1
        //                        .10" +
        //                        ".22\\byte-buddy-agent-1.10" +
        //                        ".22.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\objenesis\\objenesis\\2
        //                        .6\\objenesis-2.6" +
        //                        ".jar;" + "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\mockito\\mockito-junit-jupiter\\3.3" +
        //                        ".3\\mockito-junit-jupiter-3.3.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\skyscreamer\\jsonassert\\1.5.0\\jsonassert-1.5.0.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\vaadin\\external\\google\\android-json\\0.0.20131108" +
        //                        ".vaadin1\\android-json-0.0" +
        //                        ".20131108.vaadin1.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\xmlunit\\xmlunit-core\\2.7" +
        //                        ".0\\xmlunit-core-2.7.0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\awaitility\\awaitility\\4" +
        //                        ".0" + ".2\\awaitility-4.0.2.jar",
        //                "C:\\Users\\Oskar\\.m2\\repository\\com\\marcosbarbero\\cloud\\spring-cloud-zuul-ratelimit
        //                \\2.4.3" +
        //                        ".RELEASE\\spring-cloud-zuul-ratelimit-2.4.3.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\marcosbarbero\\cloud\\spring-cloud-zuul-ratelimit-core\\2.4
        //                        .3" +
        //                        ".RELEASE\\spring-cloud-zuul-ratelimit-core-2.4.3.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons-net\\commons-net\\3.8.0\\commons-net-3.8.0.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix-zuul\\2
        //                        .2.8" +
        //                        ".RELEASE\\spring-cloud-starter-netflix-zuul-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-zuul\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-zuul-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-hystrix\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-hystrix-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-aop\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-aop-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\httpcomponents\\httpclient\\4.5.13\\httpclient-4.5
        //                        .13.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\httpcomponents\\httpcore\\4.4
        //                        .14\\httpcore-4" +
        //                        ".4.14.jar;C:\\Users\\Oskar\\.m2\\repository\\commons-codec\\commons-codec\\1" +
        //                        ".14\\commons-codec-1.14.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\netflix-commons\\netflix-commons-util\\0.3" +
        //                        ".0\\netflix-commons-util-0.3.0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\javax\\inject\\javax" +
        //                        ".inject\\1\\javax.inject-1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-context\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-context-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-crypto\\5.3.9" +
        //                        ".RELEASE\\spring-security-crypto-5.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-commons\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-commons-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-rsa\\1.0.9" +
        //                        ".RELEASE\\spring-security-rsa-1.0.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\bouncycastle\\bcpkix-jdk15on\\1.64\\bcpkix-jdk15on-1.64.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\bouncycastle\\bcprov-jdk15on\\1
        //                        .64\\bcprov-jdk15on-1" +
        //                        ".64.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot-starter" +
        //                        "-actuator\\2.3.12.RELEASE\\spring-boot-starter-actuator-2.3.12.RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot-actuator" +
        //                        "-autoconfigure\\2.3.12.RELEASE\\spring-boot-actuator-autoconfigure-2.3.12.RELEASE
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot-actuator\\2.3.12" +
        //                        ".RELEASE\\spring-boot-actuator-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\io\\micrometer\\micrometer-core\\1.5.14\\micrometer-core-1.5.14
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\hdrhistogram\\HdrHistogram\\2.1
        //                        .12\\HdrHistogram-2.1" +
        //                        ".12.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\latencyutils\\LatencyUtils\\2.0" +
        //                        ".3\\LatencyUtils-2.0.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix-hystrix
        //                        \\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-netflix-hystrix-2.2.8.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-ribbon\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-ribbon-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix-core\\1.5.18\\hystrix-core-1.5.18
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\hystrix\\hystrix-serialization\\1
        //                        .5" +
        //                        ".18\\hystrix-serialization-1.5.18.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\module\\jackson-module-afterburner\\2.11" +
        //                        ".4\\jackson-module-afterburner-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-core\\2.11
        //                        .4\\jackson-core-2.11.4" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-annotations" +
        //                        "\\2.11.4\\jackson-annotations-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix-metrics-event-stream\\1.5" +
        //                        ".18\\hystrix-metrics-event-stream-1.5.18.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix-javanica\\1.5
        //                        .18\\hystrix-javanica-1.5.18" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\commons\\commons-lang3\\3" +
        //                        ".10\\commons-lang3-3.10.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\ow2\\asm\\asm\\5.0" +
        //                        ".4\\asm-5.0.4.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\aspectj\\aspectjweaver\\1.9" +
        //                        ".6\\aspectjweaver-1.9.6.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\google\\guava\\guava\\30" +
        //                        ".0-jre\\guava-30.0-jre.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\guava\\failureaccess\\1.0.1\\failureaccess-1.0.1
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\google\\guava\\listenablefuture\\9999" +
        //                        ".0-empty-to-avoid-conflict-with-guava\\listenablefuture-9999" +
        //                        ".0-empty-to-avoid-conflict-with-guava.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\checkerframework\\checker-qual\\3.5.0\\checker-qual-3.5.0
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\google\\errorprone\\error_prone_annotations\\2.3" +
        //                        ".4\\error_prone_annotations-2.3.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\j2objc\\j2objc-annotations\\1
        //                        .3\\j2objc-annotations-1.3.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxjava-reactive-streams\\1.2" +
        //                        ".1\\rxjava-reactive-streams-1.2.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\reactivestreams\\reactive-streams\\1.0.3\\reactive-streams-1
        //                        .0.3.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix" +
        //                        "-ribbon\\2.2.8.RELEASE\\spring-cloud-starter-netflix-ribbon-2.2.8.RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\ribbon\\ribbon\\2.3.0\\ribbon-2.3
        //                        .0.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\ribbon\\ribbon-transport\\2.3" +
        //                        ".0\\ribbon-transport-2.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\io\\reactivex\\rxnetty-contexts\\0.4.9\\rxnetty-contexts-0.4.9
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxnetty-servo\\0.4
        //                        .9\\rxnetty-servo-0.4.9" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxnetty\\0.4.9\\rxnetty-0.4
        //                        .9.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\ribbon\\ribbon-core\\2.3
        //                        .0\\ribbon-core-2.3" +
        //                        ".0.jar;C:\\Users\\Oskar\\.m2\\repository\\commons-lang\\commons-lang\\2
        //                        .6\\commons-lang-2.6" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\ribbon\\ribbon-httpclient\\2
        //                        .3" +
        //                        ".0\\ribbon-httpclient-2.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons-collections\\commons-collections\\3.2
        //                        .2\\commons-collections-3.2.2" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\com\\sun\\jersey\\jersey-client\\1.19" +
        //                        ".1\\jersey-client-1.19.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\sun\\jersey\\jersey-core\\1.19.1\\jersey-core-1.19.1.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\javax\\ws\\rs\\jsr311-api\\1.1.1\\jsr311-api-1
        //                        .1.1.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\sun\\jersey\\contribs\\jersey-apache-client4\\1.19" +
        //                        ".1\\jersey-apache-client4-1.19.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\ribbon\\ribbon-loadbalancer\\2.3
        //                        .0\\ribbon-loadbalancer-2.3.0" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\netflix\\netflix-commons\\netflix-statistics\\0" +
        //                        ".1.1\\netflix-statistics-0.1.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\io\\reactivex\\rxjava\\1.3.8\\rxjava-1.3.8.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix
        //                        -archaius\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-netflix-archaius-2.2.8.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-archaius\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-archaius-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\archaius\\archaius-core\\0.7.7\\archaius-core-0.7.7
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\google\\code\\findbugs\\jsr305\\3.0
        //                        .1\\jsr305-3.0.1" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\commons-configuration\\commons-configuration\\1" +
        //                        ".8\\commons-configuration-1.8.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\zuul\\zuul-core\\1.3.1\\zuul-core-1.3.1.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\commons-io\\commons-io\\2.4\\commons-io-2.4
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\slf4j\\slf4j-api\\1.7.30\\slf4j-api-1.7.30
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\servo\\servo-core\\0.12
        //                        .21\\servo-core-0.12" +
        //                        ".21.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot-starter" +
        //                        "-web\\2.3.12.RELEASE\\spring-boot-starter-web-2.3.12.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot\\2.3.12
        //                        .RELEASE\\spring-boot-2.3.12" +
        //                        ".RELEASE.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot" +
        //                        "-autoconfigure\\2.3.12.RELEASE\\spring-boot-autoconfigure-2.3.12.RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot-starter-logging" +
        //                        "\\2.3.12.RELEASE\\spring-boot-starter-logging-2.3.12.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\ch\\qos\\logback\\logback-classic\\1.2.3\\logback-classic-1.2.3
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\ch\\qos\\logback\\logback-core\\1.2
        //                        .3\\logback-core-1.2.3" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\apache\\logging\\log4j\\log4j-to-slf4j\\2.13" +
        //                        ".3\\log4j-to-slf4j-2.13.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\logging\\log4j\\log4j-api\\2.13.3\\log4j-api-2.13.3
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\slf4j\\jul-to-slf4j\\1.7
        //                        .30\\jul-to-slf4j-1.7.30" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\jakarta\\annotation\\jakarta
        //                        .annotation-api\\1.3" +
        //                        ".5\\jakarta.annotation-api-1.3.5.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\yaml\\snakeyaml\\1.26\\snakeyaml-1.26.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-json\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-json-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-databind\\2.11
        //                        .4\\jackson-databind-2" +
        //                        ".11.4.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\fasterxml\\jackson\\datatype\\jackson" +
        //                        "-datatype-jdk8\\2.11.4\\jackson-datatype-jdk8-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\datatype\\jackson-datatype-jsr310\\2.11" +
        //                        ".4\\jackson-datatype-jsr310-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\module\\jackson-module-parameter-names
        //                        \\2.11" +
        //                        ".4\\jackson-module-parameter-names-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-tomcat\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-tomcat-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\tomcat\\embed\\tomcat-embed-core\\9.0
        //                        .46\\tomcat-embed-core-9" +
        //                        ".0.46.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\glassfish\\jakarta.el\\3.0
        //                        .3\\jakarta.el-3" +
        //                        ".0.3.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\apache\\tomcat\\embed\\tomcat-embed" +
        //                        "-websocket\\9.0.46\\tomcat-embed-websocket-9.0.46.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-web\\5.2.15.RELEASE\\spring-web-5.2
        //                        .15.RELEASE" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-beans\\5.2.15" +
        //                        ".RELEASE\\spring-beans-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-webmvc\\5.2.15
        //                        .RELEASE\\spring-webmvc-5.2.15" +
        //                        ".RELEASE.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\spring-aop\\5.2.15" +
        //                        ".RELEASE\\spring-aop-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-context\\5.2.15
        //                        .RELEASE\\spring-context-5.2.15" +
        //                        ".RELEASE.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\spring-expression\\5.2" +
        //                        ".15.RELEASE\\spring-expression-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-core\\6.3
        //                        .0\\bucket4j-core-6.3" +
        //                        ".0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-jcache" +
        //                        "\\6.3.0\\bucket4j-jcache-6.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-hazelcast\\6.3" +
        //                        ".0\\bucket4j-hazelcast-6.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\javax\\cache\\cache-api\\1.1.1\\cache-api-1.1.1.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\hazelcast\\hazelcast\\5.0\\hazelcast-5.0.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-test\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-test-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-test\\2.3.12" +
        //                        ".RELEASE\\spring-boot-test-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-test-autoconfigure\\2.3
        //                        .12" +
        //                        ".RELEASE\\spring-boot-test-autoconfigure-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\jayway\\jsonpath\\json-path\\2.4.0\\json-path-2.4.0.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\net\\minidev\\json-smart\\2.3.1\\json-smart-2.3
        //                        .1.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\net\\minidev\\accessors-smart\\2.3
        //                        .1\\accessors-smart-2.3" +
        //                        ".1.jar;C:\\Users\\Oskar\\.m2\\repository\\jakarta\\xml\\bind\\jakarta.xml
        //                        .bind-api\\2.3" +
        //                        ".3\\jakarta.xml.bind-api-2.3.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\jakarta\\activation\\jakarta.activation-api\\1.2.2\\jakarta" +
        //                        ".activation-api-1.2.2.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\assertj\\assertj-core\\3" +
        //                        ".16.1\\assertj-core-3.16.1.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\hamcrest\\hamcrest\\2" +
        //                        ".2\\hamcrest-2.2.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\jupiter\\junit-jupiter\\5.6.3\\junit-jupiter-5.6.3
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\jupiter\\junit-jupiter-api\\5.6" +
        //                        ".3\\junit-jupiter-api-5.6.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apiguardian\\apiguardian-api\\1.1.0\\apiguardian-api-1.1.0
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\opentest4j\\opentest4j\\1.2
        //                        .0\\opentest4j-1.2.0.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\platform\\junit-platform-commons\\1
        //                        .6" +
        //                        ".3\\junit-platform-commons-1.6.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\jupiter\\junit-jupiter-params\\5.6
        //                        .3\\junit-jupiter-params-5.6" +
        //                        ".3.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\junit\\jupiter\\junit-jupiter-engine\\5.6" +
        //                        ".3\\junit-jupiter-engine-5.6.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\platform\\junit-platform-engine\\1.6
        //                        .3\\junit-platform-engine-1" +
        //                        ".6.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\mockito\\mockito-core\\3.3" +
        //                        ".3\\mockito-core-3.3.3.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\net\\bytebuddy\\byte-buddy\\1" +
        //                        ".10.22\\byte-buddy-1.10.22.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\net\\bytebuddy\\byte-buddy-agent\\1.10.22\\byte-buddy-agent-1.10
        //                        .22.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\objenesis\\objenesis\\2.6\\objenesis-2.6
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\mockito\\mockito-junit-jupiter\\3.3" +
        //                        ".3\\mockito-junit-jupiter-3.3.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\skyscreamer\\jsonassert\\1.5.0\\jsonassert-1.5.0.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\vaadin\\external\\google\\android-json\\0
        //                        .0.20131108" +
        //                        ".vaadin1\\android-json-0.0.20131108.vaadin1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-core\\5.2.15.RELEASE\\spring-core-5
        //                        .2.15" +
        //                        ".RELEASE.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\spring-jcl\\5.2.15" +
        //                        ".RELEASE\\spring-jcl-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-test\\5.2.15.RELEASE\\spring-test-5
        //                        .2.15" +
        //                        ".RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\xmlunit\\xmlunit-core\\2.7" +
        //                        ".0\\xmlunit-core-2.7.0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\awaitility\\awaitility\\4" +
        //                        ".0.2\\awaitility-4.0.2.jar",
        //                "C:\\Users\\Oskar\\.m2\\repository\\com\\marcosbarbero\\cloud\\spring-cloud-zuul-ratelimit
        //                \\2.4.3" +
        //                        ".RELEASE\\spring-cloud-zuul-ratelimit-2.4.3.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\marcosbarbero\\cloud\\spring-cloud-zuul-ratelimit-core\\2.4
        //                        .3" +
        //                        ".RELEASE\\spring-cloud-zuul-ratelimit-core-2.4.3.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons-net\\commons-net\\3.8.0\\commons-net-3.8.0.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix-zuul\\2
        //                        .2.8" +
        //                        ".RELEASE\\spring-cloud-starter-netflix-zuul-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-zuul\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-zuul-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-hystrix\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-hystrix-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-aop\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-aop-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\httpcomponents\\httpclient\\4.5.13\\httpclient-4.5
        //                        .13.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\httpcomponents\\httpcore\\4.4
        //                        .14\\httpcore-4" +
        //                        ".4.14" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\commons-codec\\commons-codec\\1
        //                        .14\\commons-codec-1" +
        //                        ".14.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\netflix\\netflix-commons\\netflix-commons-util\\0.3" +
        //                        ".0\\netflix-commons-util-0.3.0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\javax\\inject\\javax" +
        //                        ".inject\\1\\javax.inject-1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-context\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-context-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-crypto\\5.3.9" +
        //                        ".RELEASE\\spring-security-crypto-5.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-commons\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-commons-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-rsa\\1.0.9" +
        //                        ".RELEASE\\spring-security-rsa-1.0.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\bouncycastle\\bcpkix-jdk15on\\1.64\\bcpkix-jdk15on-1.64.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\bouncycastle\\bcprov-jdk15on\\1.64\\bcprov-jdk15on-1.64.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-actuator\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-actuator-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-actuator-autoconfigure\\2
        //                        .3.12" +
        //                        ".RELEASE\\spring-boot-actuator-autoconfigure-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-actuator\\2.3.12" +
        //                        ".RELEASE\\spring-boot-actuator-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\io\\micrometer\\micrometer-core\\1.5.14\\micrometer-core-1.5.14
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\hdrhistogram\\HdrHistogram\\2.1
        //                        .12\\HdrHistogram-2.1" +
        //                        ".12.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\latencyutils\\LatencyUtils\\2.0
        //                        .3\\LatencyUtils-2.0" +
        //                        ".3.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix" +
        //                        "-hystrix" +
        //                        "\\2.2.8.RELEASE\\spring-cloud-starter-netflix-hystrix-2.2.8.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-ribbon\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-ribbon-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix-core\\1.5.18\\hystrix-core-1.5.18
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\hystrix\\hystrix-serialization\\1
        //                        .5" +
        //                        ".18\\hystrix-serialization-1.5.18.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\module\\jackson-module-afterburner\\2.11" +
        //                        ".4\\jackson-module-afterburner-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-core\\2.11
        //                        .4\\jackson-core-2.11.4" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-annotations\\2.11" +
        //                        ".4\\jackson-annotations-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix-metrics-event-stream\\1.5" +
        //                        ".18\\hystrix-metrics-event-stream-1.5.18.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix-javanica\\1.5
        //                        .18\\hystrix-javanica-1.5.18" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\commons\\commons-lang3\\3" +
        //                        ".10\\commons-lang3-3.10" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\ow2\\asm\\asm\\5.0.4\\asm-5.0.4.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\aspectj\\aspectjweaver\\1.9.6\\aspectjweaver-1.9.6.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\guava\\guava\\30.0-jre\\guava-30.0-jre.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\guava\\failureaccess\\1.0.1\\failureaccess-1.0.1
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\google\\guava\\listenablefuture\\9999" +
        //                        ".0-empty-to-avoid-conflict-with-guava\\listenablefuture-9999" +
        //                        ".0-empty-to-avoid-conflict-with-guava" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\checkerframework\\checker-qual\\3.5" +
        //                        ".0\\checker-qual-3.5" +
        //                        ".0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\google\\errorprone\\error_prone_annotations" +
        //                        "\\2.3" +
        //                        ".4\\error_prone_annotations-2.3.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\j2objc\\j2objc-annotations\\1
        //                        .3\\j2objc-annotations-1.3.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxjava-reactive-streams\\1.2" +
        //                        ".1\\rxjava-reactive-streams-1.2.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\reactivestreams\\reactive-streams\\1.0.3\\reactive-streams-1
        //                        .0.3.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix" +
        //                        "-ribbon" +
        //                        "\\2.2.8.RELEASE\\spring-cloud-starter-netflix-ribbon-2.2.8.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\ribbon\\ribbon\\2.3.0\\ribbon-2.3.0.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\ribbon\\ribbon-transport\\2.3.0\\ribbon-transport-2
        //                        .3.0.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxnetty-contexts\\0.4" +
        //                        ".9\\rxnetty-contexts-0.4.9" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxnetty-servo\\0.4
        //                        .9\\rxnetty-servo-0" +
        //                        ".4.9" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxnetty\\0.4.9\\rxnetty-0.4
        //                        .9.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\ribbon\\ribbon-core\\2.3
        //                        .0\\ribbon-core-2.3" +
        //                        ".0.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\commons-lang\\commons-lang\\2.6\\commons-lang-2
        //                        .6.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\ribbon\\ribbon-httpclient\\2.3" +
        //                        ".0\\ribbon-httpclient-2.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons-collections\\commons-collections\\3.2
        //                        .2\\commons-collections-3.2.2" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\sun\\jersey\\jersey-client\\1.19
        //                        .1\\jersey-client-1" +
        //                        ".19.1" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\com\\sun\\jersey\\jersey-core\\1.19
        //                        .1\\jersey-core-1" +
        //                        ".19.1" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\javax\\ws\\rs\\jsr311-api\\1.1
        //                        .1\\jsr311-api-1.1.1" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\sun\\jersey\\contribs\\jersey-apache-client4\\1.19" +
        //                        ".1\\jersey-apache-client4-1.19.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\ribbon\\ribbon-loadbalancer\\2.3
        //                        .0\\ribbon-loadbalancer-2.3.0" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\netflix\\netflix-commons\\netflix-statistics\\0.1" +
        //                        ".1\\netflix-statistics-0.1.1.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\io\\reactivex\\rxjava\\1" +
        //                        ".3" +
        //                        ".8\\rxjava-1.3.8.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix
        //                        -archaius\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-netflix-archaius-2.2.8.RELEASE.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-archaius\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-archaius-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\archaius\\archaius-core\\0.7.7\\archaius-core-0.7.7
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\google\\code\\findbugs\\jsr305\\3.0
        //                        .1\\jsr305-3.0.1" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\commons-configuration\\commons-configuration\\1" +
        //                        ".8\\commons-configuration-1.8.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\zuul\\zuul-core\\1.3.1\\zuul-core-1.3.1.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons-io\\commons-io\\2.4\\commons-io-2.4.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\slf4j\\slf4j-api\\1.7.30\\slf4j-api-1.7.30.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\servo\\servo-core\\0.12.21\\servo-core-0.12.21.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-web\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-web-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot\\2.3.12
        //                        .RELEASE\\spring-boot-2.3.12" +
        //                        ".RELEASE" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot" +
        //                         "-autoconfigure\\2.3" +
        //                        ".12.RELEASE\\spring-boot-autoconfigure-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-logging\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-logging-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\ch\\qos\\logback\\logback-classic\\1.2.3\\logback-classic-1.2.3
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\ch\\qos\\logback\\logback-core\\1.2
        //                        .3\\logback-core-1.2.3" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\logging\\log4j\\log4j-to-slf4j\\2
        //                        .13" +
        //                        ".3\\log4j-to-slf4j-2.13.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\logging\\log4j\\log4j-api\\2.13.3\\log4j-api-2.13.3
        //                        .jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\slf4j\\jul-to-slf4j\\1.7
        //                        .30\\jul-to-slf4j-1.7.30" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\jakarta\\annotation\\jakarta.annotation-api\\1
        //                        .3" +
        //                        ".5\\jakarta" +
        //                        ".annotation-api-1.3.5.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\yaml\\snakeyaml\\1" +
        //                         ".26\\snakeyaml-1" +
        //                        ".26.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot-starter" +
        //                        "-json\\2.3" +
        //                        ".12.RELEASE\\spring-boot-starter-json-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-databind\\2.11
        //                        .4\\jackson-databind-2" +
        //                        ".11.4" +
        //                        ".jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\fasterxml\\jackson\\datatype\\jackson-datatype" +
        //                         "-jdk8\\2" +
        //                        ".11.4\\jackson-datatype-jdk8-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\datatype\\jackson-datatype-jsr310\\2.11" +
        //                        ".4\\jackson-datatype-jsr310-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\module\\jackson-module-parameter-names
        //                        \\2.11" +
        //                        ".4\\jackson-module-parameter-names-2.11.4.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-tomcat\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-tomcat-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\tomcat\\embed\\tomcat-embed-core\\9.0
        //                        .46\\tomcat-embed-core-9" +
        //                        ".0.46" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\glassfish\\jakarta.el\\3.0.3\\jakarta
        //                        .el-3.0.3" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\apache\\tomcat\\embed\\tomcat-embed-websocket\\9.0" +
        //                        ".46\\tomcat-embed-websocket-9.0.46.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-web\\5.2.15.RELEASE\\spring-web-5.2
        //                        .15.RELEASE" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-beans\\5.2.15" +
        //                        ".RELEASE\\spring-beans-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-webmvc\\5.2.15
        //                        .RELEASE\\spring-webmvc-5.2.15" +
        //                        ".RELEASE" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-aop\\5.2.15" +
        //                        ".RELEASE\\spring-aop-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-context\\5.2.15
        //                        .RELEASE\\spring-context-5.2.15" +
        //                        ".RELEASE" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-expression\\5
        //                        .2.15" +
        //                        ".RELEASE\\spring-expression-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-core\\6.3
        //                        .0\\bucket4j-core-6.3" +
        //                        ".0.jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-jcache\\6.3" +
        //                        ".0\\bucket4j-jcache-6.3.0.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-ignite\\6.3" +
        //                        ".0\\bucket4j-ignite-6.3.0" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\javax\\cache\\cache-api\\1.1
        //                        .1\\cache-api-1.1.1.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\ignite\\ignite-core\\2.11
        //                        .0\\ignite-core-2" +
        //                        ".11.0.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\jetbrains\\annotations\\16.0
        //                        .3\\annotations-16.0.3" +
        //                         ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\gridgain\\ignite-shmem\\1.0
        //                        .0\\ignite-shmem-1.0.0" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot-starter-test\\2.3" +
        //                        ".12" +
        //                        ".RELEASE\\spring-boot-starter-test-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-test\\2.3.12" +
        //                         ".RELEASE\\spring-boot-test-2.3" +
        //                        ".12.RELEASE.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\springframework\\boot\\spring-boot" +
        //                        "-test" +
        //                        "-autoconfigure\\2.3.12.RELEASE\\spring-boot-test-autoconfigure-2.3.12.RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\jayway\\jsonpath\\json-path\\2.4.0\\json-path-2.4.0.jar;" +
        //                         "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\net\\minidev\\json-smart\\2.3.1\\json-smart-2.3.1.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\net\\minidev\\accessors-smart\\2.3.1\\accessors-smart-2.3.1.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\jakarta\\xml\\bind\\jakarta.xml.bind-api\\2.3.3\\jakarta.xml
        //                        .bind-api-2.3.3" +
        //                         ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\jakarta\\activation\\jakarta.activation-api\\1
        //                        .2" +
        //                        ".2\\jakarta" +
        //                        ".activation-api-1.2.2.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\assertj\\assertj-core\\3" +
        //                        ".16" +
        //                        ".1\\assertj-core-3.16.1.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\hamcrest\\hamcrest\\2" +
        //                        ".2\\hamcrest-2.2.jar;C:\\Users\\Oskar\\" +
        //                         ".m2\\repository\\org\\junit\\jupiter\\junit-jupiter\\5.6" +
        //                        ".3\\junit-jupiter-5.6.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\jupiter\\junit-jupiter-api\\5.6
        //                        .3\\junit-jupiter-api-5.6.3.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apiguardian\\apiguardian-api\\1.1" +
        //                         ".0\\apiguardian-api-1.1.0" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\opentest4j\\opentest4j\\1.2
        //                        .0\\opentest4j-1.2.0" +
        //                         ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\platform\\junit-platform-commons\\1
        //                        .6" +
        //                        ".3\\junit-platform-commons-1.6.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\jupiter\\junit-jupiter-params\\5.6
        //                        .3\\junit-jupiter-params-5.6" +
        //                        ".3.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\jupiter\\junit-jupiter-engine\\5.6" +
        //                        ".3\\junit-jupiter-engine-5.6.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\junit\\platform\\junit-platform-engine\\1.6
        //                        .3\\junit-platform-engine-1" +
        //                        ".6.3" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\mockito\\mockito-core\\3.3
        //                        .3\\mockito-core-3.3" +
        //                         ".3.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\net\\bytebuddy\\byte-buddy\\1.10
        //                        .22\\byte-buddy-1.10.22" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\net\\bytebuddy\\byte-buddy-agent\\1.10" +
        //                        ".22\\byte-buddy-agent-1.10" +
        //                        ".22.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\objenesis\\objenesis\\2
        //                        .6\\objenesis-2.6" +
        //                         ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\mockito\\mockito-junit-jupiter\\3.3" +
        //                        ".3\\mockito-junit-jupiter-3.3.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\skyscreamer\\jsonassert\\1.5.0\\jsonassert-1.5.0.jar;" +
        //                         "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\vaadin\\external\\google\\android-json\\0.0.20131108" +
        //                        ".vaadin1\\android-json-0.0" +
        //                        ".20131108.vaadin1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-core\\5.2.15" +
        //                        ".RELEASE\\spring-core-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\spring-jcl\\5.2.15.RELEASE\\spring-jcl-5.2
        //                        .15.RELEASE" +
        //                        ".jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-test\\5.2.15" +
        //                        ".RELEASE\\spring-test-5" +
        //                        ".2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\xmlunit\\xmlunit-core\\2
        //                        .7" +
        //                        ".0\\xmlunit-core-2.7.0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\org\\awaitility\\awaitility\\4" +
        //                        ".0" +
        //                        ".2\\awaitility-4.0.2.jar",
        //                "C:\\Users\\Oskar\\.m2\\repository\\com\\marcosbarbero\\cloud\\spring-cloud-zuul-ratelimit
        //                \\2.4.3" +
        //                        ".RELEASE\\spring-cloud-zuul-ratelimit-2.4.3.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\marcosbarbero\\cloud\\spring-cloud-zuul-ratelimit-core\\2.4
        //                        .3" +
        //                        ".RELEASE\\spring-cloud-zuul-ratelimit-core-2.4.3.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons-net\\commons-net\\3.8.0\\commons-net-3.8.0.jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix-zuul\\2
        //                        .2.8" +
        //                        ".RELEASE\\spring-cloud-starter-netflix-zuul-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-zuul\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-zuul-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-netflix-hystrix\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-hystrix-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-aop\\2.3.12" +
        //                        ".RELEASE\\spring-boot-starter-aop-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\apache\\httpcomponents\\httpclient\\4.5.13\\httpclient-4.5
        //                        .13.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\httpcomponents\\httpcore\\4.4
        //                        .14\\httpcore-4" +
        //                        ".4" +
        //                        ".14.jar;C:\\Users\\Oskar\\.m2\\repository\\commons-codec\\commons-codec\\1" +
        //                        ".14\\commons-codec-1.14.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\netflix-commons\\netflix-commons-util\\0.3" +
        //                        ".0\\netflix-commons-util-0.3.0.jar;C:\\Users\\Oskar\\
        //                        .m2\\repository\\javax\\inject\\javax" +
        //                        ".inject\\1\\javax.inject-1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-context\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-context-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-crypto\\5.3.9" +
        //                        ".RELEASE\\spring-security-crypto-5.3.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-commons\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-commons-2.2.8.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\security\\spring-security-rsa\\1.0.9" +
        //                        ".RELEASE\\spring-security-rsa-1.0.9.RELEASE.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\bouncycastle\\bcpkix-jdk15on\\1.64\\bcpkix-jdk15on-1.64" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\org\\bouncycastle\\bcprov-jdk15on\\1" +
        //                        ".64\\bcprov-jdk15on-1.64.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\boot\\spring-boot-starter-actuator" +
        //                        "\\2.3.12.RELEASE\\spring-boot-starter-actuator-2.3.12.RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring" +
        //                        "-boot-actuator-autoconfigure\\2.3.12" +
        //                        ".RELEASE\\spring-boot-actuator-autoconfigure-2.3.12.RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring" +
        //                        "-boot-actuator\\2.3.12.RELEASE\\spring-boot-actuator-2.3.12.RELEASE" +
        //                        ".jar;C:\\Users\\Oskar\\.m2\\repository\\io\\micrometer\\micrometer" +
        //                        "-core\\1.5.14\\micrometer-core-1.5.14.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\hdrhistogram\\HdrHistogram\\2.1" +
        //                        ".12\\HdrHistogram-2.1.12.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\latencyutils\\LatencyUtils\\2.0" +
        //                        ".3\\LatencyUtils-2.0.3.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud" +
        //                        "-starter-netflix-hystrix\\2.2.8" +
        //                        ".RELEASE\\spring-cloud-starter-netflix-hystrix-2.2.8.RELEASE" + ".jar;
        //                        C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud" + "-netflix-ribbon\\2
        //                        .2.8" +
        //                        ".RELEASE\\spring-cloud-netflix-ribbon-2.2.8.RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix" +
        //                        "\\hystrix\\hystrix-core\\1.5.18\\hystrix-core-1.5.18" + ".jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\netflix\\hystrix\\hystrix" +
        //                        "-serialization\\1.5.18\\hystrix-serialization-1.5" + ".18.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson\\module" +
        //                        "\\jackson-module-afterburner\\2.11" +
        //                        ".4\\jackson-module-afterburner-2.11.4.jar;" + "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\fasterxml\\jackson" + "\\core\\jackson-core\\2.11
        //                        .4\\jackson-core-2" +
        //                        ".11.4.jar;C:\\Users\\Oskar\\" + ".m2\\repository\\com\\fasterxml\\jackson" +
        //                        "\\core\\jackson-annotations\\2.11" + ".4\\jackson-annotations-2.11.4.jar;" +
        //                        "C:\\Users\\Oskar\\" + ".m2\\repository\\com\\netflix\\hystrix" +
        //                        "\\hystrix-metrics-event-stream\\1.5" + ".18\\hystrix-metrics-event-stream-1" +
        //                        ".5.18.jar;C:\\Users\\Oskar\\" + ".m2\\repository\\com\\netflix" +
        //                        "\\hystrix\\hystrix-javanica\\1.5" + ".18\\hystrix-javanica-1.5.18" +
        //                        ".jar;C:\\Users\\Oskar\\" + ".m2\\repository\\org\\apache" +
        //                        "\\commons\\commons-lang3\\3" +
        //                        ".10\\commons-lang3-3.10.jar;" + "C:\\Users\\Oskar\\" + ".m2\\repository\\org\\ow2" +
        //                        "\\asm\\asm\\5.0.4\\asm-5" + ".0.4.jar;" + "C:\\Users\\Oskar\\" + "
        //                        .m2\\repository\\org" +
        //                        "\\aspectj" + "\\aspectjweaver\\1.9" + ".6\\aspectjweaver-1" + ".9.6.jar;" +
        //                        "C:\\Users\\Oskar" +
        //                        "\\.m2\\repository\\com\\google\\guava\\guava\\30.0-jre\\guava-30.0-jre.jar;" +
        //                        "C:\\Users\\Oskar\\.m2\\repository\\com\\google\\guava\\failureaccess\\1.0" +
        //                          ".1\\failureaccess-1.0.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google\\guava\\listenablefuture\\9999" +
        //                            ".0-empty-to-avoid-conflict-with-guava\\listenablefuture-9999" +
        //                             ".0-empty-to-avoid-conflict-with-guava.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\checkerframework\\checker-qual\\3.5.0\\checker-qual-3.5.0
        //                        .jar;" +
        //                               "C:\\Users\\Oskar\\
        //                               .m2\\repository\\com\\google\\errorprone\\error_prone_annotations" +
        //                                "\\2.3.4\\error_prone_annotations-2.3.4.jar;C:\\Users\\Oskar\\" +
        //                                 ".m2\\repository\\com\\google\\j2objc\\j2objc-annotations\\1
        //                                 .3\\j2objc-annotations-1" +
        //                        ".3.jar;C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxjava-reactive-streams" +
        //                        "\\1.2.1\\rxjava-reactive-streams-1.2.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud\\spring-cloud-starter-netflix" +
        //                                     "-ribbon\\2.2.8.RELEASE\\spring-cloud-starter-netflix-ribbon-2.2.8
        //                                     .RELEASE.jar;" +
        //                                      "C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\ribbon\\ribbon\\2.3" +
        //                                       ".0\\ribbon-2.3.0.jar;C:\\Users\\Oskar\\" +
        //                                        ".m2\\repository\\com\\netflix\\ribbon\\ribbon-transport\\2.3" +
        //                        ".0\\ribbon-transport-2.3.0.jar;C:\\Users\\Oskar\\" +
        //                                          ".m2\\repository\\io\\reactivex\\rxnetty-contexts\\0.4" +
        //                                           ".9\\rxnetty-contexts-0.4.9.jar;C:\\Users\\Oskar\\" +
        //                                            ".m2\\repository\\io\\reactivex\\rxnetty-servo\\0.4
        //                                            .9\\rxnetty-servo-0.4" +
        //                                             ".9.jar;C:\\Users\\Oskar\\
        //                                             .m2\\repository\\io\\reactivex\\rxnetty\\0.4" +
        //                                              ".9\\rxnetty-0.4.9.jar;C:\\Users\\Oskar\\" +
        //                                               ".m2\\repository\\com\\netflix\\ribbon\\ribbon-core\\2.3" +
        //                                                ".0\\ribbon-core-2.3.0.jar;C:\\Users\\Oskar\\" +
        //                                                 ".m2\\repository\\commons-lang\\commons-lang\\2
        //                                                 .6\\commons-lang-2.6" +
        //                                                  ".jar;C:\\Users\\Oskar\\
        //                                                  .m2\\repository\\com\\netflix\\ribbon" +
        //                                                   "\\ribbon-httpclient\\2.3.0\\ribbon-httpclient-2.3.0
        //                                                   .jar;" +
        //                                                    "C:\\Users\\Oskar\\
        //                                                    .m2\\repository\\commons-collections\\commons" +
        //                                                     "-collections\\3.2.2\\commons-collections-3.2.2.jar;" +
        //                                                      "C:\\Users\\Oskar\\
        //                                                      .m2\\repository\\com\\sun\\jersey\\jersey" +
        //                                                       "-client\\1.19.1\\jersey-client-1.19.1.jar;
        //                                                       C:\\Users\\Oskar\\" +
        //                                                        ".m2\\repository\\com\\sun\\jersey\\jersey-core\\1
        //                                                        .19" +
        //                                                         ".1\\jersey-core-1.19.1.jar;C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\javax\\ws\\rs\\jsr311-api\\1.1" +
        //                                                           ".1\\jsr311-api-1.1.1.jar;C:\\Users\\Oskar\\" +
        //                                                            "
        //                                                            .m2\\repository\\com\\sun\\jersey\\contribs\\jersey" +
        //                                                             "-apache-client4\\1.19
        //                                                             .1\\jersey-apache-client4-1.19.1" +
        //                                                              ".jar;C:\\Users\\Oskar\\" +
        //                                                               "
        //                                                               .m2\\repository\\com\\netflix\\ribbon\\ribbon" +
        //                                                                "-loadbalancer\\2.3
        //                                                                .0\\ribbon-loadbalancer-2.3.0.jar;" +
        //                                                                 "C:\\Users\\Oskar\\
        //                                                                 .m2\\repository\\com\\netflix" +
        //                                                                  "\\netflix-commons\\netflix-statistics\\0
        //                                                                  .1" +
        //                        ".1\\netflix-statistics-0.1.1.jar;" +
        //                                                                    "C:\\Users\\Oskar\\
        //                                                                    .m2\\repository\\io\\reactivex" +
        //                                                                     "\\rxjava\\1.3.8\\rxjava-1.3.8.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\org\\springframework\\cloud" +
        //                                                                        "\\spring-cloud-starter-netflix-archaius\\2.2" +
        //                                                                         ".8
        //                                                                         .RELEASE\\spring-cloud-starter-netflix" +
        //                        "-archaius-2.2.8.RELEASE.jar;" +
        //                                                                           "C:\\Users\\Oskar\\" +
        //                                                                            "
        //                                                                            .m2\\repository\\org\\springframework" +
        //                                                                             "\\cloud\\spring-cloud-netflix-archaius" +
        //                        "\\2.2.8.RELEASE\\spring-cloud-netflix" +
        //                        "-archaius-2.2.8.RELEASE.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                                                                                 "
        //                                                                                 .m2\\repository\\com\\netflix" +
        //                        "\\archaius\\archaius-core\\0.7" +
        //                                                                                   ".7\\archaius-core-0.7.7
        //                                                                                   .jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com\\google" +
        //                        "\\code\\findbugs\\jsr305\\3.0" +
        //                        ".1\\jsr305-3.0.1.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\commons" +
        //                        "-configuration\\commons" +
        //                        "-configuration\\1" +
        //                                                                                            ".8\\commons" +
        //                                                                                             "-configuration-1.8.jar;" +
        //                        "C:\\Users\\Oskar\\" +
        //                        ".m2\\repository\\com" +
        //                                                                                                "\\netflix\\zuul" +
        //                                                                                                 "\\zuul-core\\1.3" +
        //                                                                                                  "
        //                                                                                                  .1\\zuul-core-1.3" +
        //                                                                                                   ".1.jar;" +
        //                                                                                                    "C:\\Users\\Oskar" +
        //                                                                                                     "\\
        //                                                                                                     .m2\\repository\\commons-io\\commons-io\\2.4\\commons-io-2.4.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\slf4j\\slf4j-api\\1.7.30\\slf4j-api-1.7.30.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\netflix\\servo\\servo-core\\0.12.21\\servo-core-0.12.21.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot-starter-web\\2.3.12.RELEASE\\spring-boot-starter-web-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot-starter\\2.3.12.RELEASE\\spring-boot-starter-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot\\2.3.12.RELEASE\\spring-boot-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot-autoconfigure\\2.3.12.RELEASE\\spring-boot-autoconfigure-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot-starter-logging\\2.3.12.RELEASE\\spring-boot-starter-logging-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\ch\\qos\\logback\\logback-classic\\1.2.3\\logback-classic-1.2.3.jar;C:\\Users\\Oskar\\.m2\\repository\\ch\\qos\\logback\\logback-core\\1.2.3\\logback-core-1.2.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\logging\\log4j\\log4j-to-slf4j\\2.13.3\\log4j-to-slf4j-2.13.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\logging\\log4j\\log4j-api\\2.13.3\\log4j-api-2.13.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\slf4j\\jul-to-slf4j\\1.7.30\\jul-to-slf4j-1.7.30.jar;C:\\Users\\Oskar\\.m2\\repository\\jakarta\\annotation\\jakarta.annotation-api\\1.3.5\\jakarta.annotation-api-1.3.5.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\yaml\\snakeyaml\\1.26\\snakeyaml-1.26.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot-starter-json\\2.3.12.RELEASE\\spring-boot-starter-json-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\fasterxml\\jackson\\core\\jackson-databind\\2.11.4\\jackson-databind-2.11.4.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\fasterxml\\jackson\\datatype\\jackson-datatype-jdk8\\2.11.4\\jackson-datatype-jdk8-2.11.4.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\fasterxml\\jackson\\datatype\\jackson-datatype-jsr310\\2.11.4\\jackson-datatype-jsr310-2.11.4.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\fasterxml\\jackson\\module\\jackson-module-parameter-names\\2.11.4\\jackson-module-parameter-names-2.11.4.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot-starter-tomcat\\2.3.12.RELEASE\\spring-boot-starter-tomcat-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\tomcat\\embed\\tomcat-embed-core\\9.0.46\\tomcat-embed-core-9.0.46.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\glassfish\\jakarta.el\\3.0.3\\jakarta.el-3.0.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\apache\\tomcat\\embed\\tomcat-embed-websocket\\9.0.46\\tomcat-embed-websocket-9.0.46.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-web\\5.2.15.RELEASE\\spring-web-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-beans\\5.2.15.RELEASE\\spring-beans-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-webmvc\\5.2.15.RELEASE\\spring-webmvc-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-aop\\5.2.15.RELEASE\\spring-aop-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-context\\5.2.15.RELEASE\\spring-context-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-expression\\5.2.15.RELEASE\\spring-expression-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-core\\6.3.0\\bucket4j-core-6.3.0.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-jcache\\6.3.0\\bucket4j-jcache-6.3.0.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\github\\vladimir-bukhtoyarov\\bucket4j-infinispan\\6.3.0\\bucket4j-infinispan-6.3.0.jar;C:\\Users\\Oskar\\.m2\\repository\\javax\\cache\\cache-api\\1.1.1\\cache-api-1.1.1.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\infinispan\\infinispan-core\\13.0.1.Final\\infinispan-core-13.0.1.Final.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\infinispan\\protostream\\protostream\\4.4.1.Final\\protostream-4.4.1.Final.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\infinispan\\protostream\\protostream-types\\4.4.1.Final\\protostream-types-4.4.1.Final.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\jgroups\\jgroups\\4.2.17.Final\\jgroups-4.2.17.Final.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\github\\ben-manes\\caffeine\\caffeine\\2.8.8\\caffeine-2.8.8.jar;C:\\Users\\Oskar\\.m2\\repository\\jakarta\\transaction\\jakarta.transaction-api\\1.3.3\\jakarta.transaction-api-1.3.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\jboss\\logging\\jboss-logging\\3.4.2.Final\\jboss-logging-3.4.2.Final.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\jboss\\threads\\jboss-threads\\2.3.3.Final\\jboss-threads-2.3.3.Final.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\wildfly\\common\\wildfly-common\\1.3.0.Final\\wildfly-common-1.3.0.Final.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\infinispan\\infinispan-commons\\13.0.1.Final\\infinispan-commons-13.0.1.Final.jar;C:\\Users\\Oskar\\.m2\\repository\\io\\reactivex\\rxjava3\\rxjava\\3.0.4\\rxjava-3.0.4.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\reactivestreams\\reactive-streams\\1.0.3\\reactive-streams-1.0.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot-starter-test\\2.3.12.RELEASE\\spring-boot-starter-test-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot-test\\2.3.12.RELEASE\\spring-boot-test-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\boot\\spring-boot-test-autoconfigure\\2.3.12.RELEASE\\spring-boot-test-autoconfigure-2.3.12.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\jayway\\jsonpath\\json-path\\2.4.0\\json-path-2.4.0.jar;C:\\Users\\Oskar\\.m2\\repository\\net\\minidev\\json-smart\\2.3.1\\json-smart-2.3.1.jar;C:\\Users\\Oskar\\.m2\\repository\\net\\minidev\\accessors-smart\\2.3.1\\accessors-smart-2.3.1.jar;C:\\Users\\Oskar\\.m2\\repository\\jakarta\\xml\\bind\\jakarta.xml.bind-api\\2.3.3\\jakarta.xml.bind-api-2.3.3.jar;C:\\Users\\Oskar\\.m2\\repository\\jakarta\\activation\\jakarta.activation-api\\1.2.2\\jakarta.activation-api-1.2.2.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\assertj\\assertj-core\\3.16.1\\assertj-core-3.16.1.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\hamcrest\\hamcrest\\2.2\\hamcrest-2.2.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\jupiter\\junit-jupiter\\5.6.3\\junit-jupiter-5.6.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\jupiter\\junit-jupiter-api\\5.6.3\\junit-jupiter-api-5.6.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\apiguardian\\apiguardian-api\\1.1.0\\apiguardian-api-1.1.0.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\opentest4j\\opentest4j\\1.2.0\\opentest4j-1.2.0.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\platform\\junit-platform-commons\\1.6.3\\junit-platform-commons-1.6.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\jupiter\\junit-jupiter-params\\5.6.3\\junit-jupiter-params-5.6.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\jupiter\\junit-jupiter-engine\\5.6.3\\junit-jupiter-engine-5.6.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\junit\\platform\\junit-platform-engine\\1.6.3\\junit-platform-engine-1.6.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\mockito\\mockito-core\\3.3.3\\mockito-core-3.3.3.jar;C:\\Users\\Oskar\\.m2\\repository\\net\\bytebuddy\\byte-buddy\\1.10.22\\byte-buddy-1.10.22.jar;C:\\Users\\Oskar\\.m2\\repository\\net\\bytebuddy\\byte-buddy-agent\\1.10.22\\byte-buddy-agent-1.10.22.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\objenesis\\objenesis\\2.6\\objenesis-2.6.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\mockito\\mockito-junit-jupiter\\3.3.3\\mockito-junit-jupiter-3.3.3.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\skyscreamer\\jsonassert\\1.5.0\\jsonassert-1.5.0.jar;C:\\Users\\Oskar\\.m2\\repository\\com\\vaadin\\external\\google\\android-json\\0.0.20131108.vaadin1\\android-json-0.0.20131108.vaadin1.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-core\\5.2.15.RELEASE\\spring-core-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-jcl\\5.2.15.RELEASE\\spring-jcl-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\springframework\\spring-test\\5.2.15.RELEASE\\spring-test-5.2.15.RELEASE.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\xmlunit\\xmlunit-core\\2.7.0\\xmlunit-core-2.7.0.jar;C:\\Users\\Oskar\\.m2\\repository\\org\\awaitility\\awaitility\\4.0.2\\awaitility-4.0.2.jar"};
        parseArguments(args);
        MultiCompilationScheduler scheduler =
                startInsertion(parameters.neo4j_mode, parameters.neo4j_host, parameters.neo4j_port_number,
                        parameters.neo4j_user, parameters.neo4j_password, parameters.neo4j_database,
                        parameters.max_operations_transaction, parameters.programId, parameters.userId,
                        parameters.sourceFolder);

        //        String[] sources = parameters.sourceFolder.split(";");
        //        for (int i = 0; i < sources.length; i++)
        scheduler.newCompilationTask(parameters.sourceFolder, parameters.class_path);

        //        scheduler.newCompilationTask(parameters.sourceFolder+"Bis", "");

        scheduler.endAnalysis();

        System.exit(0);
    }

    public static MultiCompilationScheduler startInsertion(String neo4jMode, String neo4jHost, String neo4jPort,
                                                           String neo4jUser, String neo4jPass, String database,
                                                           String maxOps, String programId, String userId,
                                                           String dbFolder) {
        final String LOCAL_MODE = OptionsConfiguration.neo4j_modeNames[0];
        try {
            DatabaseFachade.init(neo4jMode.contentEquals(OptionsConfiguration.DEFAULT_NEO4J_MODE) ?
                    new Neo4jDriverLazyInsertion(neo4jHost, neo4jPort, neo4jUser, neo4jPass, database, maxOps) :
                    neo4jMode.contentEquals(LOCAL_MODE) ? new EmbeddedInsertion(
                            Paths.get(new File(dbFolder).getCanonicalPath(), "target", database).toAbsolutePath()
                                    .toString()) : new NotPersistentLazyInsertion());
        } catch (IOException e) {
            e.printStackTrace();
            final int ABNORMAL_TERMINATION = -1;
            System.exit(ABNORMAL_TERMINATION);
        }
        return new MultiCompilationScheduler(programId, userId);
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
