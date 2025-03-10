package dev.luisoliveira.roteiro.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PromptBuilder {

    // Constantes com dados da análise
    private static final String[] PALAVRAS_CHAVE = {
            "PODEROSA", "PROTECCIÓN", "BENDICIONES", "TRANSFORMAR", "RENOVAR"
    };

    private static final String[] TEMAS_EFICAZES = {
            "protección familiar", "mensajes diarios", "renovación financiera", "transformación personal"
    };

    public static String buildTitlePrompt(String tema, String estilo, String idioma) {
        log.debug("Construindo prompt para títulos: tema={}, estilo={}, idioma={}", tema, estilo, idioma);

        StringBuilder prompt = new StringBuilder();

        // Selecionar idioma para o prompt
        if ("pt".equalsIgnoreCase(idioma) || "pt-BR".equalsIgnoreCase(idioma)) {
            prompt.append("Crie 5 títulos em português para uma oração sobre \"")
                    .append(tema)
                    .append("\" no estilo \"")
                    .append(estilo)
                    .append("\".\n\n");
        } else if ("en".equalsIgnoreCase(idioma)) {
            prompt.append("Create 5 titles in English for a prayer about \"")
                    .append(tema)
                    .append("\" in the style \"")
                    .append(estilo)
                    .append("\".\n\n");
        } else if ("es-MX".equalsIgnoreCase(idioma)) {
            prompt.append("Crea 5 títulos en español latino/mexicano para una oración sobre \"")
                    .append(tema)
                    .append("\" en el estilo \"")
                    .append(estilo)
                    .append("\".\n\n")
                    .append("Utiliza expresiones, palabras y giros típicos del español de México y Latinoamérica.\n\n");
        } else {
            // Padrão: espanhol
            prompt.append("Crea 5 títulos en español para una oración sobre \"")
                    .append(tema)
                    .append("\" en el estilo \"")
                    .append(estilo)
                    .append("\".\n\n");
        }

        prompt.append("Use esta fórmula de título que tem o melhor CTR (5,75%):\n");
        prompt.append("[PODEROSA ORACIÓN] para [tema específico] [benefício específico] #oración #hashtag\n\n");

        prompt.append("Diretrizes importantes baseadas em análise de dados:\n");
        prompt.append("1. Use 5-8 palavras no título (CTR médio: 3,07%)\n");
        prompt.append("2. SEMPRE inclua hashtags no título (dobram visualizações)\n");
        prompt.append("3. Use MAIÚSCULAS para palavras-chave importantes\n");
        prompt.append("4. Inclua 1-2 emojis estratégicos no final\n");
        prompt.append("5. Use palavras de poder como: PODEROSA, PROTECCIÓN, BENDICIONES\n\n");

        prompt.append("Exemplos de títulos de sucesso:\n");
        prompt.append("- PODEROSA ORACION POR LOS HIJOS POR LA PROTECCIÓN #oración #salmo91\n");
        prompt.append("- Dios Tiene un Mensaje Para Ti Hoy! Escucha Esta Oración Matutina #Oración\n");
        prompt.append("- ORACIÓN PODEROSA para RENOVAR TUS FINANZAS #feyesperanza\n\n");

        prompt.append("Forneça apenas a lista de 5 títulos, sem comentários adicionais.");

        return prompt.toString();
    }

    public static String buildOracaoPrompt(String tema, String estilo, String duracao, String titulo, String idioma) {
        // Verificar parâmetros de entrada
        if (tema == null || estilo == null || duracao == null || titulo == null) {
            log.error("Parâmetros inválidos para buildOracaoPrompt: tema={}, estilo={}, duracao={}, titulo={}",
                    tema, estilo, duracao, titulo);
            throw new IllegalArgumentException("Todos os parâmetros são obrigatórios para construir o prompt da oração");
        }

        log.debug("Construindo prompt para oração: tema={}, estilo={}, duracao={}, titulo={}, idioma={}",
                tema, estilo, duracao, titulo, idioma);

        StringBuilder prompt = new StringBuilder();

        // Selecionar idioma para o prompt
        if ("pt".equalsIgnoreCase(idioma) || "pt-BR".equalsIgnoreCase(idioma)) {
            prompt.append("Crie uma oração em português com o título: \"").append(titulo).append("\"\n\n");
        } else if ("en".equalsIgnoreCase(idioma)) {
            prompt.append("Create a prayer in English with the title: \"").append(titulo).append("\"\n\n");
        } else if ("es-MX".equalsIgnoreCase(idioma)) {
            prompt.append("Crea una oración en español latino/mexicano con el título: \"").append(titulo).append("\"\n\n");
            prompt.append("Utiliza expresiones, palabras y giros típicos del español de México y Latinoamérica. Evita términos o expresiones propias del español de España.\n\n");
        } else {
            // Padrão: espanhol
            prompt.append("Crea una oración en español con el título: \"").append(titulo).append("\"\n\n");
        }

        prompt.append("Tema: ").append(tema).append("\n");
        prompt.append("Estilo: ").append(estilo).append("\n");
        prompt.append("Duração: ").append(duracao).append("\n\n");

        prompt.append("A oração deve seguir esta estrutura:\n");
        prompt.append("1. Versículo inicial relevante (\"Versículo del día:\")\n");
        prompt.append("2. Introdução com saudação e relevância do tema (6-8 linhas)\n");
        prompt.append("3. Reflexão bíblica com passagem relevante (6-8 linhas)\n");
        prompt.append("4. Oração principal com:\n");
        prompt.append("   - Invocação inicial\n");
        prompt.append("   - Desenvolvimento do tema\n");
        prompt.append("   - Aplicação prática\n");
        prompt.append("   - Pedidos específicos\n");
        prompt.append("   - Declaração de fé\n");
        prompt.append("5. Encerramento com:\n");
        prompt.append("   - Recapitulação\n");
        prompt.append("   - Versículo final (diferente do inicial)\n");
        prompt.append("   - Bênção final\n");
        prompt.append("   - Convite para inscrição\n");
        prompt.append("   - \"En el nombre de Jesús, Amén\"\n\n");

        // Ajustar tamanho baseado na duração
        if (duracao.contains("Short1")) {
            prompt.append("Tamanho total: 300-500 caracteres (30-60 segundos)");
        } else if (duracao.contains("Short2")) {
            prompt.append("Tamanho total: 500-800 caracteres (60-90 segundos)");
        } else if (duracao.contains("Mini")) {
            prompt.append("Tamanho total: 800-1.200 caracteres (2-3 minutos)");
        } else if (duracao.contains("Padrão")) {
            prompt.append("Tamanho total: 1.800-2.200 caracteres (5 minutos)");
        } else if (duracao.contains("Completa")) {
            prompt.append("Tamanho total: 3.500-4.000 caracteres (8-10 minutos)");
        } else if (duracao.contains("Expandida")) {
            prompt.append("Tamanho total: 6.000-12.000 caracteres (15+ minutos)");
        } else {
            // Formato ideal baseado na análise
            prompt.append("Tamanho total: 5.000-6.000 caracteres (15-30 minutos)");
        }

        String promptStr = prompt.toString();
        log.debug("Prompt construído com sucesso (tamanho: {} caracteres)", promptStr.length());
        return promptStr;
    }

    public static String buildShortPrompt(String oracaoContent, String titulo, String idioma) {
        log.debug("Construindo prompt para versão short: titulo={}, tamanho da oração={} caracteres, idioma={}",
                titulo, oracaoContent.length(), idioma);

        StringBuilder prompt = new StringBuilder();

        // Selecionar idioma para o prompt
        if ("pt".equalsIgnoreCase(idioma) || "pt-BR".equalsIgnoreCase(idioma)) {
            prompt.append("Crie uma versão curta (30-60 segundos) em português da seguinte oração:\n\n");
        } else if ("en".equalsIgnoreCase(idioma)) {
            prompt.append("Create a short version (30-60 seconds) in English of the following prayer:\n\n");
        } else if ("es-MX".equalsIgnoreCase(idioma)) {
            prompt.append("Crea una versión corta (30-60 segundos) en español latino/mexicano de la siguiente oración:\n\n");
            prompt.append("Utiliza expresiones, palabras y giros típicos del español de México y Latinoamérica. Evita términos o expresiones propias del español de España.\n\n");
        } else {
            // Padrão: espanhol
            prompt.append("Crea una versión corta (30-60 segundos) en español de la siguiente oración:\n\n");
        }

        prompt.append("Título original: \"").append(titulo).append("\"\n\n");
        prompt.append("Oração original:\n").append(oracaoContent).append("\n\n");

        prompt.append("Diretrizes para a versão short:\n");
        prompt.append("1. Manter entre 300-500 caracteres (ideal para vídeos de 30-60 segundos)\n");
        prompt.append("2. Preservar a essência e mensagem principal\n");
        prompt.append("3. Incluir uma invocação breve, mensagem central e encerramento\n");
        prompt.append("4. Manter pelo menos um versículo bíblico relevante\n");
        prompt.append("5. Usar linguagem direta e impactante\n");
        prompt.append("6. Terminar com \"En el nombre de Jesús, Amén\"\n\n");

        prompt.append("IMPORTANTE: A versão curta deve estar no mesmo idioma da oração original (");
        if ("pt".equalsIgnoreCase(idioma) || "pt-BR".equalsIgnoreCase(idioma)) {
            prompt.append("português");
        } else if ("en".equalsIgnoreCase(idioma)) {
            prompt.append("inglês");
        } else if ("es-MX".equalsIgnoreCase(idioma)) {
            prompt.append("español latino/mexicano");
        } else {
            prompt.append("español");
        }
        prompt.append(").\n\n");

        prompt.append("Responda APENAS com o texto da oração curta, sem comentários adicionais.");

        return prompt.toString();
    }

    public static String buildDescriptionPrompt(String title, String oracaoContent, String idioma) {
        log.debug("Construindo prompt para descrição: titulo={}, tamanho da oração={} caracteres, idioma={}",
                title, oracaoContent.length(), idioma);

        StringBuilder prompt = new StringBuilder();

        // Selecionar idioma para o prompt
        if ("pt".equalsIgnoreCase(idioma) || "pt-BR".equalsIgnoreCase(idioma)) {
            prompt.append("Crie uma descrição otimizada para YouTube e TikTok em português para o seguinte vídeo de oração:\n\n");
        } else if ("en".equalsIgnoreCase(idioma)) {
            prompt.append("Create an optimized description for YouTube and TikTok in English for the following prayer video:\n\n");
        } else if ("es-MX".equalsIgnoreCase(idioma)) {
            prompt.append("Crea una descripción optimizada para YouTube y TikTok en español latino/mexicano para el siguiente video de oración:\n\n");
            prompt.append("Utiliza expresiones, palabras y giros típicos del español de México y Latinoamérica. Evita términos o expresiones propias del español de España.\n\n");
        } else {
            // Padrão: espanhol
            prompt.append("Crea una descripción optimizada para YouTube y TikTok en español para el siguiente video de oración:\n\n");
        }

        prompt.append("Título: \"").append(title).append("\"\n\n");
        prompt.append("Conteúdo da oração:\n").append(oracaoContent).append("\n\n");

        prompt.append("Diretrizes para a descrição:\n");
        prompt.append("1. Escreva entre 500-1000 caracteres\n");
        prompt.append("2. Inclua 5-7 hashtags relevantes ao final\n");
        prompt.append("3. Adicione 3-5 frases inspiradoras relacionadas ao tema\n");
        prompt.append("4. Inclua um versículo bíblico principal\n");
        prompt.append("5. Adicione um call-to-action para inscrição/compartilhamento\n");
        prompt.append("6. Inclua 2-3 emojis estrategicamente colocados\n");
        prompt.append("7. Mencione os benefícios de ouvir esta oração\n\n");

        prompt.append("IMPORTANTE: A descrição deve estar no mesmo idioma da oração (");
        if ("pt".equalsIgnoreCase(idioma) || "pt-BR".equalsIgnoreCase(idioma)) {
            prompt.append("português");
        } else if ("en".equalsIgnoreCase(idioma)) {
            prompt.append("inglês");
        } else if ("es-MX".equalsIgnoreCase(idioma)) {
            prompt.append("español latino/mexicano");
        } else {
            prompt.append("español");
        }
        prompt.append(").\n\n");

        prompt.append("Responda APENAS com o texto da descrição, sem comentários adicionais.");

        return prompt.toString();
    }

    public static String buildImagePromptPrompt(String title, String oracaoContent, String idioma) {
        log.debug("Construindo prompt para imagem: titulo={}, tamanho da oração={} caracteres, idioma={}",
                title, oracaoContent.length(), idioma);

        StringBuilder prompt = new StringBuilder();

        // Selecionar idioma para o prompt
        if ("pt".equalsIgnoreCase(idioma) || "pt-BR".equalsIgnoreCase(idioma)) {
            prompt.append("Crie um prompt para geração de imagem em português para a miniatura deste vídeo de oração:\n\n");
        } else if ("en".equalsIgnoreCase(idioma)) {
            prompt.append("Create an image generation prompt in English for the thumbnail of this prayer video:\n\n");
        } else if ("es-MX".equalsIgnoreCase(idioma)) {
            prompt.append("Crea un prompt para generación de imagen en español latino/mexicano para la miniatura de este video de oración:\n\n");
        } else {
            // Padrão: espanhol
            prompt.append("Crea un prompt para generación de imagen en español para la miniatura de este video de oración:\n\n");
        }

        prompt.append("Título: \"").append(title).append("\"\n\n");
        prompt.append("Conteúdo da oração:\n").append(oracaoContent).append("\n\n");

        prompt.append("Diretrizes para o prompt de imagem:\n");
        prompt.append("1. Descreva uma cena inspiradora relacionada ao tema da oração\n");
        prompt.append("2. Foque em elementos visuais como luz, céu, natureza, mãos em oração, etc.\n");
        prompt.append("3. Sugira elementos que transmitam paz, esperança, fé ou o tema específico\n");
        prompt.append("4. Inclua referências a cores que combinam com o tema emocional\n");
        prompt.append("5. Evite mencionar texto na imagem - será adicionado depois\n");
        prompt.append("6. Formate como um prompt para Midjourney ou DALL-E\n\n");

        prompt.append("Responda APENAS com o prompt para geração de imagem, sem comentários adicionais.");

        return prompt.toString();
    }
}