package ch.hsr.zedcontrol.roborio.parsing;

/**
 * Interface that defines the minimal structure of a parsed object.
 */
public interface ParserData {

    /**
     * KeyWord for this ParserData object as defined in {@link KeyWords}
     *
     * @return A type of enum {@link KeyWords}
     */
    KeyWords getKeyWord();

    /**
     * String description of the parsed object.
     *
     * @return Object described as string.
     */
    String getDescription();
}
