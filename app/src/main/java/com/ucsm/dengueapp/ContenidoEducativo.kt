package com.ucsm.dengueapp

data class Video(
    val id: String,
    val titulo: String,
    val categoria: String,
    val youtubeId: String,
    val fuente: String
)

data class SeccionInfo(
    val titulo: String,
    val contenido: List<String>
)

data class Etapa(
    val nombre: String,
    val nombreCorto: String,
    val cuando: String,
    val resumen: String,
    val quePasa: List<String>,
    val queHacer: List<String>,
    val cuandoAcudir: String
)

data class FilaComparacion(
    val aspecto: String,
    val dengue: String,
    val comun: String
)

object Contenido {

    // Videos educativos oficiales de la OPS/OMS (Organización Panamericana de la Salud).
    // Fuente: https://www.paho.org/es/campanas/semana-accion-contra-mosquitos-2024
    // Para agregar otro video: copia un bloque Video(...) y cambia id, título, categoría y youtubeId
    // (el youtubeId es lo que va después de "watch?v=" en el enlace de YouTube).
    val videos = listOf(
        Video(
            id = "v1",
            titulo = "Síntomas del dengue",
            categoria = "Síntomas y signos de alarma",
            youtubeId = "FIEoX4SUtSc",
            fuente = "OPS/OMS"
        ),
        Video(
            id = "v2",
            titulo = "Signos de alarma del dengue",
            categoria = "Síntomas y signos de alarma",
            youtubeId = "RL-iZcvVh0Q",
            fuente = "OPS/OMS"
        ),
        Video(
            id = "v3",
            titulo = "Cuidados del dengue en la casa",
            categoria = "Cuidados en casa",
            youtubeId = "6zc6hnkcTDo",
            fuente = "OPS/OMS"
        ),
        Video(
            id = "v4",
            titulo = "Diez pasos para eliminar los criaderos de mosquitos y protejamos a nuestra comunidad del dengue",
            categoria = "Prevención",
            youtubeId = "_xjWTYRcJ8A",
            fuente = "OPS/OMS"
        ),
    )

    // Información sobre el dengue (REVISAR con profesionales de salud antes de usar con participantes)
    val informacionGeneral = listOf(
        SeccionInfo(
            titulo = "¿Qué es el dengue?",
            contenido = listOf(
                "El dengue es una enfermedad causada por un virus que se transmite por la picadura del mosquito Aedes aegypti.",
                "No se contagia de persona a persona, ni por agua, ni por contacto con animales.",
                "Existen cuatro serotipos del virus del dengue (DEN-1, DEN-2, DEN-3 y DEN-4)."
            )
        ),
        SeccionInfo(
            titulo = "Síntomas frecuentes",
            contenido = listOf(
                "Fiebre alta (puede llegar a 40 °C)",
                "Dolor de cabeza intenso",
                "Dolor detrás de los ojos",
                "Dolor muscular y de articulaciones (\"fiebre quebrantahuesos\")",
                "Náuseas, vómitos o pérdida de apetito",
                "Enrojecimiento de la piel o sarpullido"
            )
        )
    )

    val signosAlarma = listOf(
        SeccionInfo(
            titulo = "Signos de alarma (acude de inmediato a un establecimiento de salud)",
            contenido = listOf(
                "Dolor abdominal intenso y continuo",
                "Vómitos persistentes (más de 3 en 1 hora o más de 6 en 6 horas)",
                "Sangrado de encías, nariz, orina o heces",
                "Somnolencia o irritabilidad excesiva (especialmente en niños)",
                "Dificultad para respirar",
                "Piel fría o pálida, o sensación de mareo al levantarse",
                "Disminución importante de la orina"
            )
        ),
        SeccionInfo(
            titulo = "Importante",
            contenido = listOf(
                "Estos signos pueden indicar dengue grave. No esperes a que empeore.",
                "Si tienes fiebre y vives o viajaste a una zona con dengue, consulta temprano.",
                "Evita automedicarte con aspirina o ibuprofeno (pueden aumentar el riesgo de sangrado). Usa solo paracetamol si es necesario y siguiendo indicación médica."
            )
        )
    )

    val prevencion = listOf(
        SeccionInfo(
            titulo = "Cómo se reproduce el mosquito Aedes aegypti",
            contenido = listOf(
                "El Aedes aegypti pone sus huevos en recipientes con agua limpia y estancada.",
                "Los principales criaderos están dentro y alrededor de la casa: baldes, tanques, floreros, bebederos, llantas, botellas, platos de macetas, etc.",
                "Los huevos pueden resistir la sequía varios meses y eclosionar cuando vuelve a haber agua."
            )
        ),
        SeccionInfo(
            titulo = "Medidas de prevención en el hogar",
            contenido = listOf(
                "Lavar, escobillar y tapar tanques, baldes y recipientes donde se almacena agua.",
                "Cambiar diariamente el agua de bebederos de mascotas y lavar el recipiente.",
                "Cambiar el agua de floreros y platos de macetas; mejor usar arena húmeda.",
                "Eliminar, voltear o guardar bajo techo las llantas, botellas, latas y cualquier objeto que acumule agua.",
                "Mantener patios y jardines limpios, sin basura ni recipientes abandonados.",
                "Usar mallas o mosquiteros en puertas y ventanas.",
                "Usar repelente siguiendo las indicaciones del producto, especialmente en horas de mayor actividad del mosquito (mañana y atardecer)."
            )
        )
    )

    // Etapas de la enfermedad (información educativa; VALIDAR con Enfermería)
    val etapas = listOf(
        Etapa(
            nombre = "Etapa 1: fase febril",
            nombreCorto = "Fiebre",
            cuando = "Primeros días: la fiebre puede durar de 2 a 7 días",
            resumen = "Empieza de forma brusca con fiebre alta y malestar general.",
            quePasa = listOf(
                "Fiebre alta de inicio repentino",
                "Dolor de cabeza intenso y dolor detrás de los ojos",
                "Dolor de músculos y articulaciones",
                "Náuseas, vómitos o pérdida de apetito",
                "Enrojecimiento de la piel o sarpullido"
            ),
            queHacer = listOf(
                "Acude a un establecimiento de salud para que te evalúen, sobre todo si vives o viajaste a una zona con dengue.",
                "Descansa y toma abundante líquido.",
                "No te automediques. Evita aspirina e ibuprofeno; usa paracetamol solo con indicación médica.",
                "Protégete de las picaduras con mosquitero o repelente, para no contagiar a otras personas a través del mosquito."
            ),
            cuandoAcudir = "Si aparece cualquier signo de alarma, acude de inmediato a un establecimiento de salud."
        ),
        Etapa(
            nombre = "Etapa 2: fase crítica",
            nombreCorto = "Crítica",
            cuando = "Cuando la fiebre empieza a bajar (suele ser entre el día 3 y el 7 de la enfermedad)",
            resumen = "Es el momento más delicado. Que baje la fiebre NO significa que ya pasó el peligro.",
            quePasa = listOf(
                "La fiebre baja, pero pueden aparecer signos de alarma.",
                "Dolor abdominal intenso y continuo",
                "Vómitos persistentes",
                "Sangrado de encías, nariz, orina o heces",
                "Mucho sueño, irritabilidad o decaimiento",
                "Piel fría o pálida, mareo al levantarse, muy poca orina",
                "Esta fase suele durar uno o dos días."
            ),
            queHacer = listOf(
                "No esperes a ver si se pasa: busca atención médica de inmediato.",
                "Si el personal de salud lo indica, sigue sus controles y recomendaciones al pie de la letra.",
                "Mantén la hidratación según te indiquen."
            ),
            cuandoAcudir = "Ante cualquier signo de alarma, acude de inmediato a un establecimiento de salud. Si hay dificultad para respirar, es una emergencia."
        ),
        Etapa(
            nombre = "Etapa 3: fase de recuperación",
            nombreCorto = "Recuperación",
            cuando = "En los días siguientes a la fase crítica",
            resumen = "El cuerpo se recupera poco a poco.",
            quePasa = listOf(
                "Mejora el apetito y el estado general",
                "Puede aparecer picazón o sarpullido",
                "El cansancio puede durar varios días o semanas"
            ),
            queHacer = listOf(
                "Descansa y retoma tus actividades poco a poco.",
                "Alimentación completa y abundante líquido.",
                "Asiste a los controles que te indique el personal de salud.",
                "Sigue eliminando criaderos en casa para que nadie más se contagie."
            ),
            cuandoAcudir = "Si vuelve la fiebre, o aparece algún signo de alarma o dificultad para respirar, acude de inmediato a un establecimiento de salud."
        )
    )

    // Comparación mosquito del dengue vs. zancudo común (información general; VALIDAR con Enfermería)
    val comparacion = listOf(
        FilaComparacion(
            aspecto = "Cómo se ve",
            dengue = "Cuerpo oscuro con rayas o manchas blancas en las patas y una marca blanca en forma de lira en la parte de arriba del tórax.",
            comun = "Color pardo o marrón más uniforme, sin rayas blancas marcadas en las patas."
        ),
        FilaComparacion(
            aspecto = "Cuándo pica",
            dengue = "De día, sobre todo temprano en la mañana y al atardecer.",
            comun = "Sobre todo al anochecer y de noche."
        ),
        FilaComparacion(
            aspecto = "Cómo suena",
            dengue = "Suele volar casi en silencio.",
            comun = "Suele oírse su zumbido cerca de los oídos."
        ),
        FilaComparacion(
            aspecto = "Dónde se cría",
            dengue = "En agua limpia y estancada dentro y alrededor de la casa: baldes, tanques, floreros, llantas.",
            comun = "En aguas sucias o estancadas: charcos, desagües, canaletas, pozas."
        ),
        FilaComparacion(
            aspecto = "Enfermedades",
            dengue = "Puede transmitir dengue, chikungunya y zika.",
            comun = "No transmite dengue. Puede transmitir otras enfermedades según la zona."
        )
    )

    val consejosContraPicaduras = listOf(
        "Usa mallas o mosquiteros en puertas y ventanas.",
        "Usa repelente siguiendo las indicaciones del producto.",
        "Usa ropa que cubra brazos y piernas, sobre todo en la mañana y al atardecer.",
        "Elimina el agua acumulada: es donde ambos mosquitos se reproducen."
    )

    // Lista de revisión del hogar (checklist)
    val checklist = listOf(
        "Tanques, baldes y ollas con agua" to "Lavar, escobillar y mantener tapados",
        "Bebederos de mascotas" to "Cambiar el agua y lavar el recipiente",
        "Floreros y platos de macetas" to "Cambiar el agua y lavar el recipiente",
        "Llantas" to "Eliminar, voltear o guardar bajo techo",
        "Botellas, latas y baldes viejos" to "Eliminar o voltear para que no acumulen agua"
    )
}