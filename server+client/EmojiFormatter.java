import emoji4j.EmojiUtils;

class EmojiFormatter {

    String toEmoji(String input){
        try {
            String formatted = EmojiUtils.emojify(input);
            return formatted;
        }catch(NullPointerException n){
            return input;
        }
    }
    String toPlainText(String input) {
        try {
            String formatted = EmojiUtils.shortCodify(input);
            return formatted;
        } catch (NullPointerException n) {
            return input;
        }
    }
}
