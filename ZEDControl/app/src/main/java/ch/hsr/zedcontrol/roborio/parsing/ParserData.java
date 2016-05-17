package ch.hsr.zedcontrol.roborio.parsing;

/**
 * Interface that defines the minimal structure of a parsed object.
 */
public interface ParserData {

    String keyWord = null;

    /**
     * String description of the parsed object.
     * @return Object described as string.
     */
    String getDescription();
}
