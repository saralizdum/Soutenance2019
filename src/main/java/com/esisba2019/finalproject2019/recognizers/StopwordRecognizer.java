package com.esisba2019.finalproject2019.recognizers;

import com.esisba2019.finalproject2019.tokenizers.Token;
import com.esisba2019.finalproject2019.tokenizers.TokenType;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class StopwordRecognizer implements IRecognizer {

    private static final String DEFAULT_STOPWORDS =
            "a abord absolument afin ah ai aie aient aies ailleurs ainsi ait allaient allo allons allô" +
            "alors anterieur anterieure anterieures apres après as assez attendu au aucun aucune aucuns aujourd" +
            "aujourd'hui aupres auquel aura aurai auraient aurais aurait auras aurez auriez aurions aurons auront" +
            "aussi autre autrefois autrement autres autrui aux auxquelles auxquels avaient avais avait avant avec" +
            "avez aviez avions avoir avons ayant ayez ayons b bah bas basee bat beau beaucoup bien bigre bon boum bravo" +
            "brrr c car ce ceci cela celle celle-ci celle-là celles celles-ci celles-là celui celui-ci celui-là celà cent cependant" +
            "certain certaine certaines certains certes ces cet cette ceux ceux-ci ceux-là chacun chacune chaque cher chers chez chiche" +
            "chère chères ci cinq cinquantaine cinquante cinquantième cinquième combien comme comment comparable comparables compris concernant contre" +
            "d da dans de debout dedans dehors deja delà depuis dernier derniere derriere derrière des desormais desquelles desquels" +
            "dessous dessus deux deuxième deuxièmement devant devers devra devrait different differentes differents différent différente" +
            "différentes différents dire directe directement dit dite dits divers diverse diverses dix dix-huit dix-neuf dix-sept dixième" +
            "doit doivent donc dont dos douze douzième dring droite du duquel durant dès début désormais e effet egale egalement egales eh" +
            "elle elle-même elles elles-mêmes en encore enfin entre envers environ es essai est et etant etc etre eu eue eues euh eurent eus" +
            "eusse eussent eusses eussiez eussions eut eux eux-mêmes exactement excepté extenso exterieur eûmes eût eûtes f fais faisaient" +
            "faisant fait faites façon feront fi fois font force furent fus fusse fussent fusses fussiez fussions fut fûmes fût fûtes g gens" +
            "h ha haut hein hem hep hi ho holà hop hormis hors hou houp hue hui huit huitième hum hé hélas i ici il ils importe j je jusqu" +
            "jusque juste k l la laisser laquelle las le lequel les lesquelles lesquels leur leurs longtemps lors lorsque lui lui-meme lui-même";

    private Set<String> stopwords = new HashSet<String>();

    public StopwordRecognizer() {
        super();
    }

    public StopwordRecognizer(String[] stopwords) {
        this.stopwords.addAll(Arrays.asList(stopwords));
    }

    public void init() throws Exception {
        if (stopwords.size() == 0) {
            String[] stopwordArray = StringUtils.split(DEFAULT_STOPWORDS, " ");
            stopwords.addAll(Arrays.asList(stopwordArray));
        }
    }

    public List<Token> recognize(List<Token> tokens) {
        List<Token> recognizedTokens = new ArrayList<Token>();
        for (Token token : tokens) {
            if (token.getType() == TokenType.WORD) {
                if (stopwords.contains(StringUtils.lowerCase(token.getValue()))) {
                    token.setType(TokenType.STOP_WORD);
                }
            }if (token.getType() == TokenType.STOP_WORD) {
                    continue;
            }
            recognizedTokens.add(token);
        }

        return recognizedTokens;
    }

}
