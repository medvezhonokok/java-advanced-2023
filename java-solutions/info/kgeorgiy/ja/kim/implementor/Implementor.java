package info.kgeorgiy.ja.kim.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static info.kgeorgiy.ja.kim.implementor.Implementor.StandardSymbols.*;

/**
 * The class that creates the implementation of the interfaces.
 * A class that creates an implementation, .jar files according to the interface name
 * and path passed to the command line arguments.
 * Implements {@link Impler}, {@link JarImpler} interface
 *
 * @author Michael Kim (nocap239@gmail.com)
 */
public class Implementor implements Impler, JarImpler {

    /**
     * This method implements the given token class and creates a jarFile at the given path.
     *
     * @param token   the class for implementation
     * @param jarFile the path for creation of the jar file
     * @throws ImplerException an exception that is thrown when implementation of the token class is unsuccessful.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            if (jarFile.getParent() != null) {
                Files.createDirectories(jarFile.getParent());
            } else {
                throw new ImplerException("Failed at creation parent directory");
            }
            implement(token, jarFile.getParent());
            compile(token, jarFile.getParent());
            collectToJar(token, jarFile);
        } catch (IOException e) {
            throw new ImplerException("Cannot implement jar. " + e.getMessage(), e);
        }
    }

    /**
     * Collects the implementation of a given class and writes it to a jar file at the given path.
     *
     * @param token   The Class object representing the class to implement
     * @param jarFile The path to the jar file where the implementation will be written
     * @throws ImplerException If the implementation cannot be generated or the jar file cannot be written
     * @throws IOException     If an I/O error occurs while writing to the jar file
     */
    private void collectToJar(Class<?> token, Path jarFile) throws ImplerException, IOException {
        final Manifest manifest = new Manifest();
        final String where = pathToClass(token);
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (final JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            writer.putNextEntry(new ZipEntry(where));
            Files.copy(jarFile.getParent().resolve(where), writer);
        } catch (IOException e) {
            throw new ImplerException("Cannot write to " + getImplName(token, false, false) + ".jar");
        }
    }

    /**
     * Returns the path to the implementation class file for the given class.
     *
     * @param t The Class object representing the class to generate an implementation for
     * @return The path to the implementation class file
     */
    private String pathToClass(Class<?> t) {
        return getRealPackageName(t) + '/' + getImplName(t, true, false);
    }

    /**
     * Compiles the implementation class of a given token class using the system Java Compiler.
     *
     * @param token the class token for which to compile the implementation
     * @param root  the root directory where the implementation class should be saved
     * @throws ImplerException if the Java compiler could not be found or if it returns a non-zero exit code
     */
    private void compile(Class<?> token, Path root) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler, include tools.jar to classpath");
        }

        final String what = String.valueOf(root.resolve(Paths.get(
                getRealPackageName(token),
                getImplName(token, false, true)
        )));

        final String[] args = new String[]{
                "-encoding", "UTF-8",
                "-cp",
                getClassPath(token),
                what
        };

        final int exitCode = compiler.run(null, null, null, args);
        if (exitCode != 0) {
            throw new ImplerException("Compiler exit code isn't 0.");
        }
    }


    /**
     * Returns the real package name for a given class token by replacing '.' with the system file separator.
     *
     * @param token the class token for which to get the package name
     * @return the real package name
     */
    private String getRealPackageName(final Class<?> token) {
        return token.getPackageName().replace('.', '/');
    }

    /**
     * Returns the classpath for a given class token.
     *
     * @param t the class token for which to get the classpath
     * @return the classpath as a String
     * @throws AssertionError if the URI for the code source of the class token cannot be retrieved
     */
    private String getClassPath(final Class<?> t) {
        try {
            return Path.of(t.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Converts a given string to its Unicode escaped string representation.
     * If a character's Unicode code point is greater than or equal to 128,
     * then it will be replaced with a backslash-u followed by four hexadecimal digits,
     * representing the character's Unicode code point in hexadecimal.
     *
     * @param s the string to be converted
     * @return the Unicode escaped string representation of {@code s}
     */
    private String escapeUnicode(final StringBuilder s) {
        final StringBuilder escaped = new StringBuilder();
        for (char ch : s.toString().toCharArray()) {
            escaped.append(ch >= 128 ? String.format("\\u%04X", (int) ch) : ch);
        }
        return escaped.toString();
    }

    /**
     * This interface defines a set of standard symbols to be used across the program.
     * It includes characters and strings used for formatting and separating data.
     */
    @interface StandardSymbols {

        /**
         * A constant string representing the line separator used in the system.
         */
        String LINE_SEPARATOR = "\n";

        /**
         * A constant string representing the tab character used in the system.
         */
        String TAB = "\t";

        /**
         * A constant string representing the opening default bracket used in method calls.
         */
        String OPEN_DEFAULT_BRACKET = "(";

        /**
         * A constant string representing the closing default bracket used in method calls.
         */
        String CLOSING_DEFAULT_BRACKET = ")";

        /**
         * A constant string representing the opening figure bracket used in code blocks.
         */
        String OPEN_FIGURE_BRACKET = "{";

        /**
         * A constant string representing the closing figure bracket used in code blocks.
         */
        String CLOSING_FIGURE_BRACKET = "}";

        /**
         * A constant string representing the default separator used between arguments in method calls.
         */
        String DEFAULT_ARGS_SEPARATOR = ", ";
    }

    public static void main(final String... args) throws ImplerException {
        if (args == null) {
            throw new ImplerException("Null arguments are not allowed");
        } else if (args.length == 2) {
            if (args[0] == null || args[1] == null) {
                throw new ImplerException("Null arguments are not allowed");
            }
            try {
                Class<?> token = Class.forName(args[0]);
                new Implementor().implement(token, Path.of(args[1]));
            } catch (ClassNotFoundException e) {
                System.err.println("No such class found " + args[0]);
            } catch (ImplerException e) {
                System.err.println("Error: " + e.getMessage());
            }
        } else if (args.length == 3) {
            if (args[0] == null || args[1] == null || args[2] == null || !args[0].equals("-jar")) {
                throw new ImplerException("Invalid arguments: " + Arrays.toString(args));
            }
            try {
                Class<?> token = Class.forName(args[1]);
                new Implementor().implementJar(token, Path.of(args[2]));
            } catch (ClassNotFoundException e) {
                System.err.println("No such class found " + args[1]);
            }
        } else {
            throw new ImplerException("Invalid arguments length, excepted 2 or 3, found: " + args.length);
        }
    }

    /**
     * This method generates an implementation for the given interface and saves it to the specified root directory.
     * If the root directory does not exist, it will be created.
     *
     * @param token The interface for which an implementation is to be generated.
     * @param root  The directory where the generated implementation will be saved.
     * @throws ImplerException If the given token is not an interface or if there is an unexpected error during the process.
     */
    @Override
    public void implement(final Class<?> token, Path root) throws ImplerException {
        if (invalidClassType(token)) {
            throw new ImplerException(token + " isn't an interface.");
        }
        if (validPackageName(token)) root = getResolve(token, root);
        try {
            Files.createDirectories(root);
            writeJavaClass(token, root.resolve(getImplName(token, false, true)));
        } catch (IOException e) {
            throw new ImplerException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a Path object that resolves the specified class token to its root path,
     * with the real package name appended to the end of the path.
     *
     * @param token the Class<?> object representing the class to resolve
     * @param root  the root path to resolve from
     * @return the resolved Path object
     */
    private Path getResolve(final Class<?> token, final Path root) {
        return root.resolve(getRealPackageName(token));
    }

    /**
     * Check if class token package is not null.
     *
     * @param token the class token to check package.
     * @return true if current token package is not null, false other way.
     */
    private boolean validPackageName(final Class<?> token) {
        return token.getPackageName().length() > 0;
    }

    /**
     * Writes a Java class file for the given token to the specified root path.
     *
     * @param token the class token to generate a file for
     * @param where the filesystem path to write the file to
     * @throws ImplerException if the file cannot be created or written to
     */
    private void writeJavaClass(final Class<?> token, final Path where) throws ImplerException {
        try (BufferedWriter w = Files.newBufferedWriter(where)) {
            final StringBuilder source = new StringBuilder();

            writePackage(source, token);
            writeClassHeader(source, token);
            writeClassBody(source, token);
            writeClassFooter(source);

            w.write(escapeUnicode(source));
        } catch (IOException e) {
            throw new ImplerException(
                    "Cannot create implementation of" + token.getSimpleName() + ".java, caused by " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Writes class package. Writer will write class package.
     *
     * @param sb    StringBuilder to append Class package.
     * @param token Class to write package
     * @throws IOException if an I/O error occurs while writing the method.
     */
    private void writePackage(StringBuilder sb, Class<?> token) throws IOException {
        if (validPackageName(token))
            sb.append("package %s;%s".formatted(token.getPackageName(), LINE_SEPARATOR));
    }

    /**
     * Writes class package. Writer w will write class header.
     *
     * @param sb    StringBuilder to append Class Header.
     * @param token Class to write header.
     * @throws IOException if an I/O error occurs while writing the Class Header.
     */
    private void writeClassHeader(StringBuilder sb, Class<?> token) throws IOException {
        sb.append("public class %s implements %s%s".formatted(
                getImplName(token, false, false),
                token.getCanonicalName(),
                OPEN_FIGURE_BRACKET));
    }

    /**
     * Returns the implementation name of the given token.
     *
     * @param token          the class token for which to generate the implementation name
     * @param extensionClass a boolean indicating whether to include the ".class" extension in the implementation name
     * @param extensionJava  a boolean indicating whether to include the ".java" extension in the implementation name
     * @return the implementation name of the given token
     */
    private String getImplName(Class<?> token, boolean extensionClass, boolean extensionJava) {
        String extension = "";
        if (extensionClass) extension = ".class";
        if (extensionJava) extension = ".java";
        return token.getSimpleName() + "Impl" + extension;
    }

    /**
     * Writes all methods in current class.
     * Writer will write all methods, include method modifiers, exceptions and signature.
     *
     * @param sb    StringBuilder to append method exceptions.
     * @param token Class to get and write his methods.
     * @throws IOException if an I/O error occurs while writing the method.
     */
    private void writeClassBody(final StringBuilder sb, final Class<?> token) throws IOException {
        for (Method m : token.getMethods()) writeMethod(sb, m);
    }

    /**
     * Writes method.
     * Writer will write method signature, body, parameters, modifiers and exceptions.
     *
     * @param sb StringBuilder to append method exceptions.
     * @param m  Method to write method signature and body.
     */
    private void writeMethod(final StringBuilder sb, final Method m) {
        writeMethodModifiersAndReturnValueAndName(sb, m);
        writeMethodParameters(sb, m);
        writeMethodExceptions(sb, m);
        writeMethodBody(sb, m);
    }

    /**
     * Writes all method exceptions using delimiter (', ').
     *
     * @param sb StringBuilder to append method exceptions.
     * @param m  Method to write exceptions.
     */
    private void writeMethodExceptions(final StringBuilder sb, final Method m) {
        final Class<?>[] e = m.getExceptionTypes();
        if (e.length > 0) {
            sb.append(" throws ");
            writeAll(sb, Arrays.stream(e).map(Class::getCanonicalName).toArray(String[]::new));
        }
    }

    /**
     * Writes all String in current array. Writer will write all string by delimiter.
     *
     * @param sb   StringBuilder to append method modifiers and returns value
     * @param args Arguments to write using separator.
     */
    private void writeAll(final StringBuilder sb, final String[] args) {
        int i;
        for (i = 0; i < args.length - 1; i++) {
            sb.append("%s%s".formatted(args[i], DEFAULT_ARGS_SEPARATOR));
        }
        sb.append(args[i]);
    }

    /**
     * Writes method signature.
     *
     * @param sb StringBuilder to append method modifiers and returns value
     * @param m  Method to write modifiers and returns value
     */
    private void writeMethodModifiersAndReturnValueAndName(StringBuilder sb, Method m) {
        sb.append("%s %s %s"
                .formatted(
                        Modifier.toString(m.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT),
                        m.getReturnType().getCanonicalName(),
                        m.getName()));
    }

    /**
     * Writs all method parameters in circle brackets.
     *
     * @param sb StringBuilder to append method parameters
     * @param m  Method to write parameters
     */
    private void writeMethodParameters(StringBuilder sb, Method m) {
        sb.append(OPEN_DEFAULT_BRACKET);
        final Parameter[] p = m.getParameters();
        if (p.length > 0) {
            writeAll(sb, Arrays.stream(p)
                    .map(getParameterInfo())
                    .toArray(String[]::new));
        }
        sb.append(CLOSING_DEFAULT_BRACKET);
    }

    /**
     * Returns a lambda function that takes a Parameter object as input and returns a string containing the
     * canonical name of the parameter's type and its name concatenated with a space.
     *
     * @return Function object that takes a Parameter as input and returns a string representation of its type and name.
     */
    private Function<Parameter, String> getParameterInfo() {
        return parameter -> parameter.getType().getCanonicalName() + " " + parameter.getName();
    }

    /**
     * Writes an empty method body returns default value in association by default return value.
     *
     * @param sb StringBuilder to append the closing figure bracket to
     * @param m  Method to write signature
     */
    private void writeMethodBody(final StringBuilder sb, final Method m) {
        sb.append("     { return %s; }".formatted(getDefaultReturnedValue(m)));
    }

    /**
     * Generates a default returned value for a given method based on its return type.
     *
     * @param m the method for which to generate a default returned value
     * @return the default returned value for the given method
     */
    private String getDefaultReturnedValue(final Method m) {
        final Class<?> clazz = m.getReturnType();
        if (clazz.equals(void.class)) {
            return "";
        }
        if (clazz.equals(boolean.class)) {
            return "true";
        }
        if (clazz.isPrimitive()) {
            return "0";
        }
        return "null";
    }

    /**
     * Writes the closing figure bracket for the class footer.
     *
     * @param sb StringBuilder to append the closing figure bracket to
     * @throws IOException if an I/O error occurs while writing the closing figure bracket
     */
    private void writeClassFooter(final StringBuilder sb) throws IOException {
        sb.append(TAB + CLOSING_FIGURE_BRACKET);
    }

    /**
     * Checks if a given class is not an interface or this class modifiers is not private.
     *
     * @param token The class to be checked.
     * @return Returns 'true' if the class is invalid, and 'false' if it is valid.
     */
    private boolean invalidClassType(final Class<?> token) {
        return !token.isInterface() || Modifier.isPrivate(token.getModifiers());
    }
}
