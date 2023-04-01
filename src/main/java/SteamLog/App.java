package SteamLog;

import SteamLog.Connection.SteamConnectionBuilder;
import SteamLog.Connection.SteamConnectionHandler;
import SteamLog.Exceptions.SteamLogConfigException;
import SteamLog.Interpreter.StatusHandler;
import SteamLog.Utils.AppConfig;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class App {

    public static SteamConnectionHandler connection;
    public static Logger logger;
    public static List<SteamConnectionHandler> connectionHandlerPool = new ArrayList<>(2);
    public static List<StatusHandler> statusHandlerPool = new ArrayList<>(2);

    public static void initialize() {
        List<SteamConnectionBuilder> builders = new ArrayList<>(
                AppConfig.UIDS_TO_TRACK.size() + AppConfig.VANITY_IDS_TO_TRACK.size()
        );
        for (String uid : AppConfig.UIDS_TO_TRACK)
            builders.add(new SteamConnectionBuilder(
                    new SteamConnectionBuilder.UIDArgs(AppConfig.API_KEY, uid))
            );
        for (String vid : AppConfig.VANITY_IDS_TO_TRACK)
            builders.add(new SteamConnectionBuilder(
                    new SteamConnectionBuilder.VanityArgs(AppConfig.API_KEY, vid))
            );

        builders.forEach( builder -> {
                    connectionHandlerPool.add(new SteamConnectionHandler(builder));
                    statusHandlerPool.add(new StatusHandler());
                });



        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder rawMessage = new PatternLayoutEncoder();
        FileAppender<ILoggingEvent> sessionData = new FileAppender<>();
        PatternLayoutEncoder defaultPattern = new PatternLayoutEncoder();
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();

        ch.qos.logback.classic.Logger log = lc.getLogger("SteamLog");

        rawMessage.setPattern("%msg%n");
        rawMessage.setContext(lc);
        rawMessage.start();
        sessionData.setFile(AppConfig.SESSION_LOG_PATH.toString());
        sessionData.setEncoder(rawMessage);
        sessionData.setContext(lc);
        LevelFilter levelFilter = new LevelFilter();
        levelFilter.setLevel(Level.INFO);
        levelFilter.setOnMatch(FilterReply.NEUTRAL);
        levelFilter.setOnMismatch(FilterReply.DENY);
        levelFilter.start();
        sessionData.addFilter(levelFilter);
        sessionData.start();

        defaultPattern.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        defaultPattern.setContext(lc);
        defaultPattern.start();
        if (AppConfig.DEBUG_LOG_PATH != null) {
            FileAppender<ILoggingEvent> debugData = new FileAppender<>();
            debugData.setFile(AppConfig.DEBUG_LOG_PATH.toString());
            debugData.setEncoder(defaultPattern);
            debugData.setContext(lc);
            ThresholdFilter debugFilter = new ThresholdFilter();
            debugFilter.setLevel(Level.TRACE.levelStr);
            debugFilter.start();
            debugData.addFilter(debugFilter);
            debugData.start();
            log.addAppender(debugData);
        }

        consoleAppender.setEncoder(defaultPattern);
        ThresholdFilter consoleFilter = new ThresholdFilter();
        consoleFilter.setLevel(Level.INFO.toString());
        consoleFilter.start();
        consoleAppender.setContext(lc);
        consoleAppender.addFilter(consoleFilter);
        consoleAppender.start();


        log.setAdditive(false);
        log.addAppender(sessionData);
        log.addAppender(consoleAppender);
        logger = log;

    }



    public static void main(String[] args) {
        try {
            AppConfig.load();
        } catch (IOException | SteamLogConfigException e) {
            e.printStackTrace();
            System.exit(1);
        }

        initialize();

        Runtime.getRuntime().addShutdownHook( new Thread(() -> logger.info(LocalDateTime.now() + ",SHUTDOWN")) );

        class ProcessingRunnable {
            public final SteamConnectionHandler con;
            public final StatusHandler stat;
            public final Runnable runnable;

            public ProcessingRunnable(SteamConnectionHandler sch, StatusHandler sh) {
                con = sch;
                stat = sh;

                runnable = () -> {
                    while (true) {
                        stat.process(con.requestData());

                        try {
                            sleep(stat.getRefreshTime());
                        } catch (InterruptedException ignored) {
                            //Expected on shutdown
                        }
                    }
                };
            }
        }

        for (int i = 0; i < connectionHandlerPool.size(); ++i) {
            ProcessingRunnable run = new ProcessingRunnable(connectionHandlerPool.get(i), statusHandlerPool.get(i));
            new Thread(run.runnable).start();
        }

    }
}
