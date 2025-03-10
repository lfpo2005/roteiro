package dev.luisoliveira.roteiro.util;


public class PromptBuilder {

    // Constantes com dados da análise
    private static final String[] PALAVRAS_CHAVE = {
            "PODEROSA", "PROTECCIÓN", "BENDICIONES", "TRANSFORMAR", "RENOVAR"
    };

    private static final String[] TEMAS_EFICAZES = {
            "protección familiar", "mensajes diarios", "renovación financiera", "transformación personal"
    };

    public static String buildTitlePrompt(String tema, String estilo) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Crie 5 títulos em espanhol para uma oração sobre \"")
                .append(tema)
                .append("\" no estilo \"")
                .append(estilo)
                .append("\".\n\n");

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

    public static String buildOracaoPrompt(String tema, String estilo, String duracao, String titulo) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Crie uma oração em espanhol com o título: \"").append(titulo).append("\"\n\n");

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

        return prompt.toString();
    }

    public static String buildShortPrompt(String oracaoContent, String titulo) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Crie uma versão curta (30-60 segundos) da seguinte oração em espanhol:\n\n");
        prompt.append("Título original: \"").append(titulo).append("\"\n\n");
        prompt.append("Oração original:\n").append(oracaoContent).append("\n\n");

        prompt.append("Diretrizes para a versão short:\n");
        prompt.append("1. Manter entre 300-500 caracteres (ideal para vídeos de 30-60 segundos)\n");
        prompt.append("2. Preservar a essência e mensagem principal\n");
        prompt.append("3. Incluir uma invocação breve, mensagem central e encerramento\n");
        prompt.append("4. Manter pelo menos um versículo bíblico relevante\n");
        prompt.append("5. Usar linguagem direta e impactante\n");
        prompt.append("6. Terminar com \"En el nombre de Jesús, Amén\"\n\n");

        prompt.append("Responda APENAS com o texto da oração curta, sem comentários adicionais.");

        return prompt.toString();
    }

    public static String buildDescriptionPrompt(String title, String oracaoContent) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Crie uma descrição otimizada para YouTube e TikTok para o seguinte vídeo de oração:\n\n");
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

        prompt.append("Responda APENAS com o texto da descrição, sem comentários adicionais.");

        return prompt.toString();
    }

    public static String buildImagePromptPrompt(String title, String oracaoContent) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Crie um prompt para geração de imagem para a miniatura deste vídeo de oração:\n\n");
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