package ru.igla.tfprofiler.core


import org.jetbrains.annotations.NonNls
import ru.igla.tfprofiler.core.Timber.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.Collections.unmodifiableList

/** Logging for lazy people. */
class Timber private constructor() {

    object Log {
        /**
         * Priority constant for the println method; use Log.v.
         */
        val VERBOSE = 2

        /**
         * Priority constant for the println method; use Log.d.
         */
        val DEBUG = 3

        /**
         * Priority constant for the println method; use Log.i.
         */
        val INFO = 4

        /**
         * Priority constant for the println method; use Log.w.
         */
        val WARN = 5

        /**
         * Priority constant for the println method; use Log.e.
         */
        val ERROR = 6

        /**
         * Priority constant for the println method.
         */
        val ASSERT = 7
    }

    init {
        throw AssertionError()
    }

    companion object Forest : Tree() {
        /** Log a verbose message with optional format args. */
        @JvmStatic
        override fun v(@NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.v(message, *args) }
        }

        /** Log a verbose exception and a message with optional format args. */
        @JvmStatic
        override fun v(t: Throwable?, @NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.v(t, message, *args) }
        }

        /** Log a verbose exception. */
        @JvmStatic
        override fun v(t: Throwable?) {
            treeArray.forEach { it.v(t) }
        }

        /** Log a debug message with optional format args. */
        @JvmStatic
        override fun d(@NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.d(message, *args) }
        }

        /** Log a debug exception and a message with optional format args. */
        @JvmStatic
        override fun d(t: Throwable?, @NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.d(t, message, *args) }
        }

        /** Log a debug exception. */
        @JvmStatic
        override fun d(t: Throwable?) {
            treeArray.forEach { it.d(t) }
        }

        /** Log an info message with optional format args. */
        @JvmStatic
        override fun i(@NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.i(message, *args) }
        }

        /** Log an info exception and a message with optional format args. */
        @JvmStatic
        override fun i(t: Throwable?, @NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.i(t, message, *args) }
        }

        /** Log an info exception. */
        @JvmStatic
        override fun i(t: Throwable?) {
            treeArray.forEach { it.i(t) }
        }

        /** Log a warning message with optional format args. */
        @JvmStatic
        override fun w(@NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.w(message, *args) }
        }

        /** Log a warning exception and a message with optional format args. */
        @JvmStatic
        override fun w(t: Throwable?, @NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.w(t, message, *args) }
        }

        /** Log a warning exception. */
        @JvmStatic
        override fun w(t: Throwable?) {
            treeArray.forEach { it.w(t) }
        }

        /** Log an error message with optional format args. */
        @JvmStatic
        override fun e(@NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.e(message, *args) }
        }

        /** Log an error exception and a message with optional format args. */
        @JvmStatic
        override fun e(t: Throwable?, @NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.e(t, message, *args) }
        }

        /** Log an error exception. */
        @JvmStatic
        override fun e(t: Throwable?) {
            treeArray.forEach { it.e(t) }
        }

        /** Log an assert message with optional format args. */
        @JvmStatic
        override fun wtf(@NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.wtf(message, *args) }
        }

        /** Log an assert exception and a message with optional format args. */
        @JvmStatic
        override fun wtf(t: Throwable?, @NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.wtf(t, message, *args) }
        }

        /** Log an assert exception. */
        @JvmStatic
        override fun wtf(t: Throwable?) {
            treeArray.forEach { it.wtf(t) }
        }

        /** Log at `priority` a message with optional format args. */
        @JvmStatic
        override fun log(priority: Int, @NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.log(priority, message, *args) }
        }

        /** Log at `priority` an exception and a message with optional format args. */
        @JvmStatic
        override fun log(priority: Int, t: Throwable?, @NonNls message: String?, vararg args: Any?) {
            treeArray.forEach { it.log(priority, t, message, *args) }
        }

        /** Log at `priority` an exception. */
        @JvmStatic
        override fun log(priority: Int, t: Throwable?) {
            treeArray.forEach { it.log(priority, t) }
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            throw AssertionError() // Missing override for log method.
        }

        /**
         * A view into Timber's planted trees as a tree itself. This can be used for injecting a logger
         * instance rather than using static methods or to facilitate testing.
         */
        @Suppress(
                "NOTHING_TO_INLINE", // Kotlin users should reference `Tree.Forest` directly.
                "NON_FINAL_MEMBER_IN_OBJECT" // For japicmp check.
        )
        @JvmStatic
        open inline fun asTree(): Tree = this

        /** Set a one-time tag for use on the next logging call. */
        @JvmStatic
        fun tag(tag: String): Tree {
            for (tree in treeArray) {
                tree.explicitTag.set(tag)
            }
            return this
        }

        /** Add a new logging tree. */
        @JvmStatic
        fun plant(tree: Tree) {
            require(tree !== this) { "Cannot plant Timber into itself." }
            synchronized(trees) {
                trees.add(tree)
                treeArray = trees.toTypedArray()
            }
        }

        /** Adds new logging trees. */
        @JvmStatic
        fun plant(vararg trees: Tree) {
            for (tree in trees) {
                requireNotNull(tree) { "trees contained null" }
                require(tree !== this) { "Cannot plant Timber into itself." }
            }
            synchronized(this.trees) {
                Collections.addAll(this.trees, *trees)
                treeArray = this.trees.toTypedArray()
            }
        }

        /** Remove a planted tree. */
        @JvmStatic
        fun uproot(tree: Tree) {
            synchronized(trees) {
                require(trees.remove(tree)) { "Cannot uproot tree which is not planted: $tree" }
                treeArray = trees.toTypedArray()
            }
        }

        /** Remove all planted trees. */
        @JvmStatic
        fun uprootAll() {
            synchronized(trees) {
                trees.clear()
                treeArray = emptyArray()
            }
        }

        /** Return a copy of all planted [trees][Tree]. */
        @JvmStatic
        fun forest(): List<Tree> {
            synchronized(trees) {
                return unmodifiableList(trees.toList())
            }
        }

        @get:[JvmStatic JvmName("treeCount")]
        val treeCount
            get() = treeArray.size

        // Both fields guarded by 'trees'.
        private val trees = ArrayList<Tree>()
        @Volatile
        private var treeArray = emptyArray<Tree>()
    }
}

/** A facade for handling logging calls. Install instances via [`Timber.plant()`][.plant]. */
abstract class Tree {
    @get:JvmSynthetic // Hide from public API.
    internal val explicitTag = ThreadLocal<String>()

    var addStackTrace = true //to disable stacktrace
        get
        set

    @get:JvmSynthetic // Hide from public API.
    open val tag: String?
        get() {
            val tag = explicitTag.get()
            if (tag != null) {
                explicitTag.remove()
            }
            return tag
        }

    /** Log a verbose message with optional format args. */
    open fun v(message: String?, vararg args: Any?) {
        prepareLog(Log.VERBOSE, null, message, *args)
    }

    /** Log a verbose exception and a message with optional format args. */
    open fun v(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(Log.VERBOSE, t, message, *args)
    }

    /** Log a verbose exception. */
    open fun v(t: Throwable?) {
        prepareLog(Log.VERBOSE, t, null)
    }

    /** Log a debug message with optional format args. */
    open fun d(message: String?, vararg args: Any?) {
        prepareLog(Log.DEBUG, null, message, *args)
    }

    /** Log a debug exception and a message with optional format args. */
    open fun d(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(Log.DEBUG, t, message, *args)
    }

    /** Log a debug exception. */
    open fun d(t: Throwable?) {
        prepareLog(Log.DEBUG, t, null)
    }

    /** Log an info message with optional format args. */
    open fun i(message: String?, vararg args: Any?) {
        prepareLog(Log.INFO, null, message, *args)
    }

    /** Log an info exception and a message with optional format args. */
    open fun i(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(Log.INFO, t, message, *args)
    }

    /** Log an info exception. */
    open fun i(t: Throwable?) {
        prepareLog(Log.INFO, t, null)
    }

    /** Log a warning message with optional format args. */
    open fun w(message: String?, vararg args: Any?) {
        prepareLog(Log.WARN, null, message, *args)
    }

    /** Log a warning exception and a message with optional format args. */
    open fun w(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(Log.WARN, t, message, *args)
    }

    /** Log a warning exception. */
    open fun w(t: Throwable?) {
        prepareLog(Log.WARN, t, null)
    }

    /** Log an error message with optional format args. */
    open fun e(message: String?, vararg args: Any?) {
        prepareLog(Log.ERROR, null, message, *args)
    }

    /** Log an error exception and a message with optional format args. */
    open fun e(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(Log.ERROR, t, message, *args)
    }

    /** Log an error exception. */
    open fun e(t: Throwable?) {
        prepareLog(Log.ERROR, t, null)
    }

    /** Log an assert message with optional format args. */
    open fun wtf(message: String?, vararg args: Any?) {
        prepareLog(Log.ASSERT, null, message, *args)
    }

    /** Log an assert exception and a message with optional format args. */
    open fun wtf(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(Log.ASSERT, t, message, *args)
    }

    /** Log an assert exception. */
    open fun wtf(t: Throwable?) {
        prepareLog(Log.ASSERT, t, null)
    }

    /** Log at `priority` a message with optional format args. */
    open fun log(priority: Int, message: String?, vararg args: Any?) {
        prepareLog(priority, null, message, *args)
    }

    /** Log at `priority` an exception and a message with optional format args. */
    open fun log(priority: Int, t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(priority, t, message, *args)
    }

    /** Log at `priority` an exception. */
    open fun log(priority: Int, t: Throwable?) {
        prepareLog(priority, t, null)
    }

    /** Return whether a message at `priority` should be logged. */
    @Deprecated("Use isLoggable(String, int)", ReplaceWith("this.isLoggable(null, priority)"))
    protected open fun isLoggable(priority: Int) = true

    /** Return whether a message at `priority` or `tag` should be logged. */
    protected open fun isLoggable(tag: String?, priority: Int) = isLoggable(priority)

    private fun prepareLog(priority: Int, t: Throwable?, message: String?, vararg args: Any?) {
        // Consume tag even when message is not loggable so that next message is correctly tagged.
        val tag = tag
        if (!isLoggable(tag, priority)) {
            return
        }

        var message = message
        if (message.isNullOrEmpty()) {
            if (t == null) {
                return  // Swallow message if it's null and there's no throwable.
            }
            message = getStackTraceString(t)
        } else {
            if (args.isNotEmpty()) {
                message = formatMessage(message, args)
            }
            if (t != null && addStackTrace) {
                message += "\n" + getStackTraceString(t)
            }
        }

        log(priority, tag, message, t)
    }

    /** Formats a log message with optional arguments. */
    protected open fun formatMessage(message: String, args: Array<out Any?>) = message.format(*args)

    private fun getStackTraceString(t: Throwable): String {
        // Don't replace this with Log.getStackTraceString() - it hides
        // UnknownHostException, which is not what we want.
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    /**
     * Write a log message to its destination. Called for all level-specific methods by default.
     *
     * @param priority Log level. See [Log] for constants.
     * @param tag Explicit or inferred tag. May be `null`.
     * @param message Formatted log message.
     * @param t Accompanying exceptions. May be `null`.
     */
    protected abstract fun log(priority: Int, tag: String?, message: String, t: Throwable?)
}
