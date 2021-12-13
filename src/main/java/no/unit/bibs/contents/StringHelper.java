package no.unit.bibs.contents;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    private static final Pattern pattern = Pattern.compile("&(?:#x([0-9a-fA-F]+)|#([0-9]+)|([0-9A-Za-z]+));");
    public static final int OVERSIZED_7 = 7;
    public static final int OVERSIZED_6 = 6;

    private static Set<String> namedEntities = new HashSet<>();

    static {
        namedEntities.add("quot");
        namedEntities.add("amp");
        namedEntities.add("lt");
        namedEntities.add("gt");
        namedEntities.add("apos");
        namedEntities.add("nbsp");
        namedEntities.add("iexcl");
        namedEntities.add("cent");
        namedEntities.add("pound");
        namedEntities.add("curren");
        namedEntities.add("yen");
        namedEntities.add("brvbar");
        namedEntities.add("sect");
        namedEntities.add("uml");
        namedEntities.add("copy");
        namedEntities.add("ordf");
        namedEntities.add("laquo");
        namedEntities.add("not");
        namedEntities.add("shy");
        namedEntities.add("reg");
        namedEntities.add("macr");
        namedEntities.add("deg");
        namedEntities.add("plusmn");
        namedEntities.add("sup2");
        namedEntities.add("sup3");
        namedEntities.add("acute");
        namedEntities.add("micro");
        namedEntities.add("para");
        namedEntities.add("middot");
        namedEntities.add("cedil");
        namedEntities.add("sup1");
        namedEntities.add("ordm");
        namedEntities.add("raquo");
        namedEntities.add("frac14");
        namedEntities.add("frac12");
        namedEntities.add("frac34");
        namedEntities.add("iquest");
        namedEntities.add("Agrave");
        namedEntities.add("Aacute");
        namedEntities.add("Acirc");
        namedEntities.add("Atilde");
        namedEntities.add("Auml");
        namedEntities.add("Aring");
        namedEntities.add("AElig");
        namedEntities.add("Ccedil");
        namedEntities.add("Egrave");
        namedEntities.add("Eacute");
        namedEntities.add("Ecirc");
        namedEntities.add("Euml");
        namedEntities.add("Igrave");
        namedEntities.add("Iacute");
        namedEntities.add("Icirc");
        namedEntities.add("Iuml");
        namedEntities.add("ETH");
        namedEntities.add("Ntilde");
        namedEntities.add("Ograve");
        namedEntities.add("Oacute");
        namedEntities.add("Ocirc");
        namedEntities.add("Otilde");
        namedEntities.add("Ouml");
        namedEntities.add("times");
        namedEntities.add("Oslash");
        namedEntities.add("Ugrave");
        namedEntities.add("Uacute");
        namedEntities.add("Ucirc");
        namedEntities.add("Uuml");
        namedEntities.add("Yacute");
        namedEntities.add("THORN");
        namedEntities.add("szlig");
        namedEntities.add("agrave");
        namedEntities.add("aacute");
        namedEntities.add("acirc");
        namedEntities.add("atilde");
        namedEntities.add("auml");
        namedEntities.add("aring");
        namedEntities.add("aelig");
        namedEntities.add("ccedil");
        namedEntities.add("egrave");
        namedEntities.add("eacute");
        namedEntities.add("ecirc");
        namedEntities.add("euml");
        namedEntities.add("igrave");
        namedEntities.add("iacute");
        namedEntities.add("icirc");
        namedEntities.add("iuml");
        namedEntities.add("eth");
        namedEntities.add("ntilde");
        namedEntities.add("ograve");
        namedEntities.add("oacute");
        namedEntities.add("ocirc");
        namedEntities.add("otilde");
        namedEntities.add("ouml");
        namedEntities.add("divide");
        namedEntities.add("oslash");
        namedEntities.add("ugrave");
        namedEntities.add("uacute");
        namedEntities.add("ucirc");
        namedEntities.add("uuml");
        namedEntities.add("yacute");
        namedEntities.add("thorn");
        namedEntities.add("yuml");
        namedEntities.add("OElig");
        namedEntities.add("oelig");
        namedEntities.add("Scaron");
        namedEntities.add("scaron");
        namedEntities.add("Yuml");
        namedEntities.add("fnof");
        namedEntities.add("circ");
        namedEntities.add("tilde");
        namedEntities.add("Alpha");
        namedEntities.add("Beta");
        namedEntities.add("Gamma");
        namedEntities.add("Delta");
        namedEntities.add("Epsilon");
        namedEntities.add("Zeta");
        namedEntities.add("Eta");
        namedEntities.add("Theta");
        namedEntities.add("Iota");
        namedEntities.add("Kappa");
        namedEntities.add("Lambda");
        namedEntities.add("Mu");
        namedEntities.add("Nu");
        namedEntities.add("Xi");
        namedEntities.add("Omicron");
        namedEntities.add("Pi");
        namedEntities.add("Rho");
        namedEntities.add("Sigma");
        namedEntities.add("Tau");
        namedEntities.add("Upsilon");
        namedEntities.add("Phi");
        namedEntities.add("Chi");
        namedEntities.add("Psi");
        namedEntities.add("Omega");
        namedEntities.add("alpha");
        namedEntities.add("beta");
        namedEntities.add("gamma");
        namedEntities.add("delta");
        namedEntities.add("epsilon");
        namedEntities.add("zeta");
        namedEntities.add("eta");
        namedEntities.add("theta");
        namedEntities.add("iota");
        namedEntities.add("kappa");
        namedEntities.add("lambda");
        namedEntities.add("mu");
        namedEntities.add("nu");
        namedEntities.add("xi");
        namedEntities.add("omicron");
        namedEntities.add("pi");
        namedEntities.add("rho");
        namedEntities.add("sigmaf");
        namedEntities.add("sigma");
        namedEntities.add("tau");
        namedEntities.add("upsilon");
        namedEntities.add("phi");
        namedEntities.add("chi");
        namedEntities.add("psi");
        namedEntities.add("omega");
        namedEntities.add("thetasym");
        namedEntities.add("upsih");
        namedEntities.add("piv");
        namedEntities.add("ensp");
        namedEntities.add("emsp");
        namedEntities.add("thinsp");
        namedEntities.add("zwnj");
        namedEntities.add("zwj");
        namedEntities.add("lrm");
        namedEntities.add("rlm");
        namedEntities.add("ndash");
        namedEntities.add("mdash");
        namedEntities.add("lsquo");
        namedEntities.add("rsquo");
        namedEntities.add("sbquo");
        namedEntities.add("ldquo");
        namedEntities.add("rdquo");
        namedEntities.add("bdquo");
        namedEntities.add("dagger");
        namedEntities.add("Dagger");
        namedEntities.add("bull");
        namedEntities.add("hellip");
        namedEntities.add("permil");
        namedEntities.add("prime");
        namedEntities.add("Prime");
        namedEntities.add("lsaquo");
        namedEntities.add("rsaquo");
        namedEntities.add("oline");
        namedEntities.add("frasl");
        namedEntities.add("euro");
        namedEntities.add("weierp");
        namedEntities.add("image");
        namedEntities.add("real");
        namedEntities.add("trade");
        namedEntities.add("alefsym");
        namedEntities.add("larr");
        namedEntities.add("uarr");
        namedEntities.add("rarr");
        namedEntities.add("darr");
        namedEntities.add("harr");
        namedEntities.add("crarr");
        namedEntities.add("lArr");
        namedEntities.add("uArr");
        namedEntities.add("rArr");
        namedEntities.add("dArr");
        namedEntities.add("hArr");
        namedEntities.add("forall");
        namedEntities.add("part");
        namedEntities.add("exist");
        namedEntities.add("empty");
        namedEntities.add("nabla");
        namedEntities.add("isin");
        namedEntities.add("notin");
        namedEntities.add("ni");
        namedEntities.add("prod");
        namedEntities.add("sum");
        namedEntities.add("minus");
        namedEntities.add("lowast");
        namedEntities.add("radic");
        namedEntities.add("prop");
        namedEntities.add("infin");
        namedEntities.add("ang");
        namedEntities.add("and");
        namedEntities.add("or");
        namedEntities.add("cap");
        namedEntities.add("cup");
        namedEntities.add("int");
        namedEntities.add("there4");
        namedEntities.add("sim");
        namedEntities.add("cong");
        namedEntities.add("asymp");
        namedEntities.add("ne");
        namedEntities.add("equiv");
        namedEntities.add("le");
        namedEntities.add("ge");
        namedEntities.add("sub");
        namedEntities.add("sup");
        namedEntities.add("nsub");
        namedEntities.add("sube");
        namedEntities.add("supe");
        namedEntities.add("oplus");
        namedEntities.add("otimes");
        namedEntities.add("perp");
        namedEntities.add("sdot");
        namedEntities.add("lceil");
        namedEntities.add("rceil");
        namedEntities.add("lfloor");
        namedEntities.add("rfloor");
        namedEntities.add("lang");
        namedEntities.add("rang");
        namedEntities.add("loz");
        namedEntities.add("spades");
        namedEntities.add("clubs");
        namedEntities.add("hearts");
        namedEntities.add("diams");
    }

    /**
     * checks if a String contains an html escaped character.
     * @param string string to check
     * @return TRUE if the string contains an html escaped char
     */
    @SuppressWarnings("PMD.CognitiveComplexity")
    public static boolean isValidHtmlEscapeCode(String string) {
        if (string == null) {
            return false;
        }
        Matcher m = pattern.matcher(string);

        if (m.find()) {
            int codePoint = -1;
            String entity;
            try {
                String group1 = entity = m.group(1);
                if (group1 != null) {
                    if (entity.length() > OVERSIZED_6) {
                        return false;
                    }
                    codePoint = Integer.parseInt(entity, 16);
                } else {
                    String group2 = entity = m.group(2);
                    if (group2 != null) {
                        if (entity.length() > OVERSIZED_7) {
                            return false;
                        }
                        codePoint = Integer.parseInt(entity, 10);
                    } else {
                        String group3 = entity = m.group(3);
                        if (group3 != null) {
                            return namedEntities.contains(entity);
                        }
                    }
                }
                return 0x00 <= codePoint && codePoint < 0xd800
                        || 0xdfff < codePoint && codePoint <= 0x10FFFF;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
