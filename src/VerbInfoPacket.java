import java.io.Serializable;

/**
 * An encapsulated object carrying all relevant information about a Japanese verb
 *
 * @author Colin Bernstein
 * @version 1.4
 */
class VerbInfoPacket implements Serializable {
    
    private byte type, transitivity;
    private String furigana, romaji, translation;
    
    VerbInfoPacket(byte type, byte transitivity, String furigana, String romaji, String translation) {
        this.type = type;
        this.transitivity = transitivity;
        this.furigana = furigana;
        this.romaji = romaji;
        this.translation = translation;
    }
    
    byte getType() {
        return type;
    }
    
    byte getTransitivity() {
        return transitivity;
    }
    
    String getFurigana() {
        return furigana;
    }
    
    String getRomaji() {
        return romaji;
    }
    
    String getTranslation() {
        return translation;
    }
}