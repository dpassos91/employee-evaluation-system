package aor.projetofinal.Util;

import java.text.Normalizer;

public class StringUtils {
//normalizar nomes
    public static String normalize(String input) {
        if (input == null) return null;
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // Remove acentos
                .toLowerCase();           // Ignora maiúsculas/minúsculas
    }
}
