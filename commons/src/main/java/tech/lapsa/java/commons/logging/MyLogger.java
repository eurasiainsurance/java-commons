package tech.lapsa.java.commons.logging;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

import tech.lapsa.java.commons.function.MyObjects;
import tech.lapsa.java.commons.function.MyStrings;

public final class MyLogger {

    private final Logger logger;
    private final UnaryOperator<String> handler;

    private MyLogger(Logger logger, UnaryOperator<String> handler) {
	this.logger = MyObjects.requireNonNull(logger, "logger");
	this.handler = MyObjects.requireNonNull(handler, "handler");
    }

    public final class MyLevel {
	private final Level level;

	private MyLevel(Level level) {
	    this.level = level;
	}

	public MyLogger log(String message) {
	    logger.log(level, handler.apply(message));
	    return MyLogger.this;
	}

	public MyLogger log(String format, Object... args) {
	    logger.log(level, handler.apply(String.format(format, args)));
	    return MyLogger.this;
	}

	public MyLogger log(Throwable thrown, String message) {
	    return log(thrown, message);
	}

	public MyLogger log(Throwable thrown) {
	    MyObjects.requireNonNull(thrown, "thrown");
	    logger.log(level, handler.apply(thrown.getMessage()), thrown);
	    return MyLogger.this;
	}

	public MyLogger log(Throwable thrown, String format, Object... args) {
	    logger.log(level, handler.apply(String.format(format, args)), thrown);
	    return MyLogger.this;
	}
    }

    public final MyLevel CONFIG = new MyLevel(Level.CONFIG);
    public final MyLevel INFO = new MyLevel(Level.INFO);
    public final MyLevel FINE = new MyLevel(Level.FINE);
    public final MyLevel FINER = new MyLevel(Level.FINER);
    public final MyLevel FINEST = new MyLevel(Level.FINEST);
    public final MyLevel SEVERE = new MyLevel(Level.SEVERE);
    public final MyLevel WARNING = new MyLevel(Level.WARNING);
    public final MyLevel WARN = WARNING;

    public Logger logger() {
	return logger;
    }

    private final static MyLogger DEFAULT = newBuilder().build();

    public static MyLogger getDefault() {
	return DEFAULT;
    }

    public static MyLoggerBuilder newBuilder() {
	return new MyLoggerBuilder();
    }

    public static final class MyLoggerBuilder {

	private String name;

	private Function<String, String> handler;

	private MyLoggerBuilder() {
	}

	public MyLoggerBuilder withNameOf(Package pack) {
	    this.name = MyObjects.requireNonNull(pack, "pack").getName();
	    return this;
	}

	public MyLoggerBuilder withNameOf(Class<?> clazz) {
	    this.name = MyObjects.requireNonNull(clazz, "clazz").getName();
	    return this;
	}

	public MyLoggerBuilder withName(String name) {
	    this.name = MyStrings.requireNonEmpty(name, "name");
	    return this;
	}

	public MyLoggerBuilder withPackageNameOf(Class<?> clazz) {
	    this.name = MyObjects.requireNonNull(clazz, "clazz").getPackage().getName();
	    return this;
	}

	public MyLoggerBuilder addWithPrefix(String prefix) {
	    MyStrings.requireNonEmpty(prefix);
	    addAfter(x -> prefix + " : " + x);
	    return this;
	}

	public MyLoggerBuilder addWithSuffix(String suffix) {
	    MyStrings.requireNonEmpty(suffix);
	    addAfter(x -> x + " " + suffix);
	    return this;
	}

	public MyLoggerBuilder addWithCAPS() {
	    addAfter(String::toUpperCase);
	    return this;
	}

	public MyLoggerBuilder clearHandler() {
	    handler = null;
	    return this;
	}

	private void addAfter(Function<String, String> after) {
	    MyObjects.requireNonNull(after, "after");
	    handler = MyObjects.isNull(handler) //
		    ? after //
		    : handler.andThen(after);
	}

	public MyLogger build() {
	    final Logger logger = MyStrings.empty(name) //
		    ? Logger.getAnonymousLogger() //
		    : Logger.getLogger(name);

	    final UnaryOperator<String> hndlr = MyObjects.isNull(handler) //
		    ? x -> x //
		    : handler::apply;

	    return new MyLogger(logger, hndlr);
	}
    }
}
