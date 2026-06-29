package com.example.data

import java.util.UUID

enum class ComponentType(val arabicName: String, val category: String) {
    NAVBAR("شريط التنقل 🗂️", "التنقل واللوحة"),
    HERO("الواجهة الرئيسية ⭐", "العناوين والترويج"),
    FEATURES("شبكة المميزات ⚡", "العرض والخدمات"),
    TEXT_BLOCK("فقرة نصية ✍️", "المحتوى والنصوص"),
    IMAGE_BLOCK("صورة تفاعلية 🖼️", "الوسائط"),
    BUTTON_LINK("زر إجراء 🎯", "الروابط والتفاعل"),
    CARD_WIDGET("بطاقة منتج 📦", "العرض والخدمات"),
    CONTACT_FORM("نموذج اتصال ✉️", "التفاعل والاتصال"),
    FOOTER("التذييل المطور 📋", "التنقل واللوحة")
}

data class WebComponent(
    val id: String = UUID.randomUUID().toString(),
    val type: ComponentType,
    val properties: Map<String, String> = emptyMap()
) {
    companion object {
        fun createDefault(type: ComponentType): WebComponent {
            val props = when (type) {
                ComponentType.NAVBAR -> mapOf(
                    "logo" to "🚀",
                    "title" to "ويب سكيتش برو",
                    "link1" to "الرئيسية",
                    "link2" to "المميزات",
                    "link3" to "اتصل بنا",
                    "bg_color" to "#6200EE",
                    "text_color" to "#FFFFFF"
                )
                ComponentType.HERO -> mapOf(
                    "title" to "صمم موقعك بلمسة واحدة",
                    "subtitle" to "برمجة مرئية حديثة بالسحب والإفلات وتعديل مباشر للكود وتصدير فوري للملفات",
                    "btn_text" to "ابدأ الآن مجاناً",
                    "btn_link" to "#",
                    "image_url" to "https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=800",
                    "bg_color" to "#1F1F2E"
                )
                ComponentType.FEATURES -> mapOf(
                    "title1" to "سهولة السحب والإفلات",
                    "desc1" to "واجهة مرئية مرنة لترتيب العناصر وتعديل خصائصها بدقة",
                    "title2" to "تعديل الكود في الاتجاهين",
                    "desc2" to "أي تعديل في الواجهة يغير الكود، وأي تعديل في الكود يغير الواجهة",
                    "title3" to "ذكاء اصطناعي مدمج",
                    "desc3" to "اطلب من مساعد Gemini توليد صفحات كاملة أو تعديل عناصر محددة",
                    "bg_color" to "#F4F6F9"
                )
                ComponentType.TEXT_BLOCK -> mapOf(
                    "title" to "منصة ويب سكيتش برو المتكاملة",
                    "content" to "تم تصميم هذه المنصة لتمنح المبرمجين والمصممين أداة فائقة القوة لبناء نماذج أولية وتطبيقات ويب متكاملة بسرعة فائقة. تتميز المنصة بإنتاج كود نظيف وخالي من التعقيد.",
                    "align" to "center"
                )
                ComponentType.IMAGE_BLOCK -> mapOf(
                    "image_url" to "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800",
                    "caption" to "موقع ويب متجاوب مع جميع الشاشات والأجهزة",
                    "height" to "300px"
                )
                ComponentType.BUTTON_LINK -> mapOf(
                    "text" to "اضغط هنا لمشاهدة المزيد",
                    "url" to "https://google.com",
                    "bg_color" to "#FF5722",
                    "text_color" to "#FFFFFF",
                    "align" to "center"
                )
                ComponentType.CARD_WIDGET -> mapOf(
                    "title" to "منتج مميز أو خدمة",
                    "desc" to "أضف تفاصيل المنتج، الخدمة، أو العرض الخاص بك هنا بأسلوب المتاجر الحديثة.",
                    "price" to "99$ / سنوياً",
                    "image_url" to "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800",
                    "btn_text" to "طلب الخدمة الآن"
                )
                ComponentType.CONTACT_FORM -> mapOf(
                    "title" to "تواصل معنا مباشرة",
                    "desc" to "يسعدنا الرد على استفساراتك في أي وقت. املأ النموذج بالأسفل وسيتواصل معك فريقنا.",
                    "placeholder_name" to "الاسم الكامل باللغة العربية",
                    "placeholder_email" to "بريدك الإلكتروني المفضل",
                    "btn_text" to "إرسال الرسالة الآن",
                    "bg_color" to "#FFFFFF"
                )
                ComponentType.FOOTER -> mapOf(
                    "text" to "جميع الحقوق محفوظة © 2026 WebSketch Pro",
                    "link1" to "سياسة الخصوصية",
                    "link2" to "شروط الاستخدام",
                    "bg_color" to "#1A1A1A",
                    "text_color" to "#888888"
                )
            }
            return WebComponent(type = type, properties = props)
        }
    }
}

fun List<WebComponent>.toHtml(): String {
    val builder = StringBuilder()
    builder.append("""
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>موقع ويب مطور بواسطة WebSketch Pro</title>
    <link href="https://fonts.googleapis.com/css2?family=Cairo:wght@300;400;600;700;800&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Cairo', sans-serif;
            margin: 0;
            padding: 0;
            background-color: #FAFAFB;
            color: #2D3748;
            box-sizing: border-box;
            transition: all 0.3s ease;
        }
        *, *:before, *:after {
            box-sizing: inherit;
        }
        img {
            max-width: 100%;
            height: auto;
            display: block;
        }
        a {
            transition: opacity 0.25s ease, transform 0.2s ease;
        }
        a:hover {
            opacity: 0.85;
            transform: translateY(-1px);
        }
        button {
            transition: all 0.25s ease;
        }
        button:hover {
            opacity: 0.9;
            transform: scale(1.02);
        }
    </style>
</head>
<body>
    <div class="websketch-container">
""".trimIndent())

    for (comp in this) {
        val props = comp.properties
        val id = comp.id
        builder.append("\n        <!-- COMPONENT_${comp.type.name}_START -->\n")
        
        when (comp.type) {
            ComponentType.NAVBAR -> {
                val logo = props["logo"] ?: "🚀"
                val title = props["title"] ?: "موقعي"
                val link1 = props["link1"] ?: "الرئيسية"
                val link2 = props["link2"] ?: "المميزات"
                val link3 = props["link3"] ?: "اتصل بنا"
                val bgColor = props["bg_color"] ?: "#6200EE"
                val textColor = props["text_color"] ?: "#FFFFFF"
                
                builder.append("""
        <header class="websketch-navbar" id="comp-$id" data-ws-type="NAVBAR" data-ws-logo="$logo" data-ws-title="$title" data-ws-link1="$link1" data-ws-link2="$link2" data-ws-link3="$link3" data-ws-bg-color="$bgColor" data-ws-text-color="$textColor" style="background-color: $bgColor; color: $textColor; display: flex; justify-content: space-between; align-items: center; padding: 18px 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); position: sticky; top: 0; z-index: 1000;">
            <div style="font-size: 24px; font-weight: 800; display: flex; align-items: center; gap: 12px; font-family: 'Cairo', sans-serif;">
                <span style="font-size: 28px;">$logo</span>
                <span>$title</span>
            </div>
            <nav style="display: flex; gap: 24px; align-items: center;">
                <a href="#" style="color: inherit; text-decoration: none; font-weight: 600; font-size: 15px;">$link1</a>
                <a href="#" style="color: inherit; text-decoration: none; font-weight: 600; font-size: 15px;">$link2</a>
                <a href="#" style="color: inherit; text-decoration: none; font-weight: 600; font-size: 15px;">$link3</a>
            </nav>
        </header>
""".trimIndent())
            }
            ComponentType.HERO -> {
                val title = props["title"] ?: "العنوان الرئيسي"
                val subtitle = props["subtitle"] ?: "تفاصيل فرعية"
                val btnText = props["btn_text"] ?: "ابدأ"
                val btnLink = props["btn_link"] ?: "#"
                val imageUrl = props["image_url"] ?: ""
                val bgColor = props["bg_color"] ?: "#1F1F2E"
                
                builder.append("""
        <section class="websketch-hero" id="comp-$id" data-ws-type="HERO" data-ws-title="$title" data-ws-subtitle="$subtitle" data-ws-btn-text="$btnText" data-ws-btn-link="$btnLink" data-ws-image-url="$imageUrl" data-ws-bg-color="$bgColor" style="background: $bgColor; color: #FFFFFF; padding: 80px 24px; text-align: center; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 450px; gap: 20px; box-shadow: inset 0 -4px 10px rgba(0,0,0,0.15);">
            <h1 style="font-size: 44px; margin: 0; font-weight: 800; max-width: 900px; line-height: 1.4; letter-spacing: -0.5px; text-shadow: 0 2px 4px rgba(0,0,0,0.3);">$title</h1>
            <p style="font-size: 19px; margin: 0; opacity: 0.95; max-width: 700px; line-height: 1.8; font-weight: 300;">$subtitle</p>
            ${if (imageUrl.isNotEmpty()) "<img src=\"$imageUrl\" style=\"max-width: 90%; max-height: 280px; border-radius: 16px; margin: 20px 0; object-fit: cover; box-shadow: 0 10px 30px rgba(0,0,0,0.3); border: 2px solid rgba(255,255,255,0.15);\" alt=\"Hero Image\" />" else ""}
            <a href="$btnLink" style="background-color: #00E676; color: #111111; padding: 14px 40px; border-radius: 50px; text-decoration: none; font-weight: 700; font-size: 17px; box-shadow: 0 6px 15px rgba(0,230,118,0.4); display: inline-block;">$btnText</a>
        </section>
""".trimIndent())
            }
            ComponentType.FEATURES -> {
                val title1 = props["title1"] ?: "ميزة 1"
                val desc1 = props["desc1"] ?: "وصف 1"
                val title2 = props["title2"] ?: "ميزة 2"
                val desc2 = props["desc2"] ?: "وصف 2"
                val title3 = props["title3"] ?: "ميزة 3"
                val desc3 = props["desc3"] ?: "وصف 3"
                val bgColor = props["bg_color"] ?: "#F4F6F9"
                
                builder.append("""
        <div class="websketch-features" id="comp-$id" data-ws-type="FEATURES" data-ws-title1="$title1" data-ws-desc1="$desc1" data-ws-title2="$title2" data-ws-desc2="$desc2" data-ws-title3="$title3" data-ws-desc3="$desc3" data-ws-bg-color="$bgColor" style="background-color: $bgColor; padding: 75px 24px; display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 32px; text-align: center; max-width: 1200px; margin: 0 auto; width: 100%;">
            <div style="background: #FFFFFF; padding: 40px 24px; border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.03); border: 1px solid rgba(0,0,0,0.02); transition: transform 0.25s;">
                <div style="font-size: 44px; margin-bottom: 20px;">⭐</div>
                <h3 style="margin: 0 0 12px 0; font-size: 22px; color: #1A202C; font-weight: 700;">$title1</h3>
                <p style="margin: 0; color: #718096; font-size: 15px; line-height: 1.7; font-weight: 300;">$desc1</p>
            </div>
            <div style="background: #FFFFFF; padding: 40px 24px; border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.03); border: 1px solid rgba(0,0,0,0.02); transition: transform 0.25s;">
                <div style="font-size: 44px; margin-bottom: 20px;">⚡</div>
                <h3 style="margin: 0 0 12px 0; font-size: 22px; color: #1A202C; font-weight: 700;">$title2</h3>
                <p style="margin: 0; color: #718096; font-size: 15px; line-height: 1.7; font-weight: 300;">$desc2</p>
            </div>
            <div style="background: #FFFFFF; padding: 40px 24px; border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.03); border: 1px solid rgba(0,0,0,0.02); transition: transform 0.25s;">
                <div style="font-size: 44px; margin-bottom: 20px;">🤖</div>
                <h3 style="margin: 0 0 12px 0; font-size: 22px; color: #1A202C; font-weight: 700;">$title3</h3>
                <p style="margin: 0; color: #718096; font-size: 15px; line-height: 1.7; font-weight: 300;">$desc3</p>
            </div>
        </div>
""".trimIndent())
            }
            ComponentType.TEXT_BLOCK -> {
                val title = props["title"] ?: ""
                val content = props["content"] ?: ""
                val align = props["align"] ?: "center"
                
                builder.append("""
        <div class="websketch-text-block" id="comp-$id" data-ws-type="TEXT_BLOCK" data-ws-title="$title" data-ws-content="$content" data-ws-align="$align" style="padding: 60px 24px; text-align: $align; max-width: 850px; margin: 0 auto; width: 100%;">
            ${if (title.isNotEmpty()) "<h2 style=\"font-size: 32px; color: #1A202C; margin-top: 0; margin-bottom: 20px; font-weight: 700; line-height: 1.3;\">$title</h2>" else ""}
            <p style="font-size: 17px; color: #4A5568; line-height: 1.9; white-space: pre-line; font-weight: 400; margin: 0;">$content</p>
        </div>
""".trimIndent())
            }
            ComponentType.IMAGE_BLOCK -> {
                val imageUrl = props["image_url"] ?: ""
                val caption = props["caption"] ?: ""
                val height = props["height"] ?: "300px"
                
                builder.append("""
        <div class="websketch-image-block" id="comp-$id" data-ws-type="IMAGE_BLOCK" data-ws-image-url="$imageUrl" data-ws-caption="$caption" data-ws-height="$height" style="text-align: center; padding: 40px 24px; max-width: 1000px; margin: 0 auto; width: 100%;">
            <img src="${imageUrl.ifEmpty { "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800" }}" style="max-width: 100%; height: $height; object-fit: cover; border-radius: 16px; box-shadow: 0 10px 25px rgba(0,0,0,0.06); display: inline-block;" alt="Responsive visual block" />
            ${if (caption.isNotEmpty()) "<p style=\"margin-top: 14px; font-size: 15px; color: #718096; font-style: italic; font-weight: 400;\">$caption</p>" else ""}
        </div>
""".trimIndent())
            }
            ComponentType.BUTTON_LINK -> {
                val text = props["text"] ?: "زر إجراء"
                val url = props["url"] ?: "#"
                val bgColor = props["bg_color"] ?: "#FF5722"
                val textColor = props["text_color"] ?: "#FFFFFF"
                val align = props["align"] ?: "center"
                
                builder.append("""
        <div class="websketch-button-link" id="comp-$id" data-ws-type="BUTTON_LINK" data-ws-text="$text" data-ws-url="$url" data-ws-bg-color="$bgColor" data-ws-text-color="$textColor" data-ws-align="$align" style="text-align: $align; padding: 30px 24px;">
            <a href="$url" style="background-color: $bgColor; color: $textColor; padding: 14px 36px; border-radius: 10px; text-decoration: none; font-weight: 700; font-size: 16px; display: inline-block; box-shadow: 0 5px 12px rgba(0,0,0,0.1); border: 1px solid rgba(0,0,0,0.05);">$text</a>
        </div>
""".trimIndent())
            }
            ComponentType.CARD_WIDGET -> {
                val title = props["title"] ?: ""
                val desc = props["desc"] ?: ""
                val price = props["price"] ?: ""
                val imageUrl = props["image_url"] ?: ""
                val btnText = props["btn_text"] ?: "تفاصيل"
                
                builder.append("""
        <div class="websketch-card-widget" id="comp-$id" data-ws-type="CARD_WIDGET" data-ws-title="$title" data-ws-desc="$desc" data-ws-price="$price" data-ws-image-url="$imageUrl" data-ws-btn-text="$btnText" style="max-width: 420px; margin: 40px auto; background: #FFFFFF; border-radius: 20px; overflow: hidden; box-shadow: 0 12px 30px rgba(0,0,0,0.06); border: 1px solid rgba(0,0,0,0.05);">
            <img src="${imageUrl.ifEmpty { "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800" }}" style="width: 100%; height: 230px; object-fit: cover;" alt="Card display" />
            <div style="padding: 28px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; gap: 10px;">
                    <h3 style="margin: 0; font-size: 22px; color: #1A202C; font-weight: 700;">$title</h3>
                    <span style="font-weight: 800; color: #E53E3E; font-size: 19px; white-space: nowrap;">$price</span>
                </div>
                <p style="margin: 0 0 20px 0; color: #4A5568; font-size: 15px; line-height: 1.7; font-weight: 300;">$desc</p>
                <button style="width: 100%; background: #6200EE; color: #FFFFFF; border: none; padding: 12px; border-radius: 10px; font-weight: 700; font-size: 15px; cursor: pointer;">$btnText</button>
            </div>
        </div>
""".trimIndent())
            }
            ComponentType.CONTACT_FORM -> {
                val title = props["title"] ?: ""
                val desc = props["desc"] ?: ""
                val placeholderName = props["placeholder_name"] ?: "الاسم الكامل"
                val placeholderEmail = props["placeholder_email"] ?: "البريد الإلكتروني"
                val btnText = props["btn_text"] ?: "إرسال"
                val bgColor = props["bg_color"] ?: "#FFFFFF"
                
                builder.append("""
        <section class="websketch-contact-form" id="comp-$id" data-ws-type="CONTACT_FORM" data-ws-title="$title" data-ws-desc="$desc" data-ws-placeholder-name="$placeholderName" data-ws-placeholder-email="$placeholderEmail" data-ws-btn-text="$btnText" data-ws-bg-color="$bgColor" style="background-color: $bgColor; padding: 50px 32px; border-radius: 20px; max-width: 550px; margin: 40px auto; box-shadow: 0 10px 25px rgba(0,0,0,0.04); border: 1px solid rgba(0,0,0,0.05); text-align: center; width: 100%;">
            <h3 style="margin: 0 0 10px 0; font-size: 26px; color: #1A202C; font-weight: 700;">$title</h3>
            <p style="margin: 0 0 24px 0; color: #718096; font-size: 15px; line-height: 1.6;">$desc</p>
            <form onsubmit="event.preventDefault(); alert('تم إرسال رسالتك بنجاح!');" style="display: flex; flex-direction: column; gap: 14px; text-align: right;">
                <input type="text" placeholder="$placeholderName" required style="padding: 14px; border: 1.5px solid #E2E8F0; border-radius: 10px; font-size: 15px; font-family: inherit; width: 100%; box-sizing: border-box; outline: none; transition: border 0.2s;" />
                <input type="email" placeholder="$placeholderEmail" required style="padding: 14px; border: 1.5px solid #E2E8F0; border-radius: 10px; font-size: 15px; font-family: inherit; width: 100%; box-sizing: border-box; outline: none; transition: border 0.2s;" />
                <textarea placeholder="نص رسالتك بالتفصيل هنا..." required rows="4" style="padding: 14px; border: 1.5px solid #E2E8F0; border-radius: 10px; font-size: 15px; font-family: inherit; width: 100%; box-sizing: border-box; resize: vertical; outline: none; transition: border 0.2s;"></textarea>
                <button type="submit" style="background: #00C853; color: #FFFFFF; border: none; padding: 14px; border-radius: 10px; font-weight: 700; font-size: 16px; cursor: pointer; box-shadow: 0 4px 10px rgba(0,200,83,0.25); width: 100%;">$btnText</button>
            </form>
        </section>
""".trimIndent())
            }
            ComponentType.FOOTER -> {
                val text = props["text"] ?: ""
                val link1 = props["link1"] ?: "سياسة الخصوصية"
                val link2 = props["link2"] ?: "شروط الاستخدام"
                val bgColor = props["bg_color"] ?: "#1A1A1A"
                val textColor = props["text_color"] ?: "#888888"
                
                builder.append("""
        <footer class="websketch-footer" id="comp-$id" data-ws-type="FOOTER" data-ws-text="$text" data-ws-link1="$link1" data-ws-link2="$link2" data-ws-bg-color="$bgColor" data-ws-text-color="$textColor" style="background-color: $bgColor; color: $textColor; padding: 40px 24px; text-align: center; font-size: 14px; border-top: 1px solid rgba(255,255,255,0.05);">
            <p style="margin: 0 0 14px 0; font-weight: 400; line-height: 1.6;">$text</p>
            <div style="display: flex; justify-content: center; gap: 20px; align-items: center; font-weight: 500;">
                <a href="#" style="color: inherit; text-decoration: none;">$link1</a>
                <span style="opacity: 0.4;">|</span>
                <a href="#" style="color: inherit; text-decoration: none;">$link2</a>
            </div>
        </footer>
""".trimIndent())
            }
        }
        builder.append("\n        <!-- COMPONENT_${comp.type.name}_END -->\n")
    }

    builder.append("""
    </div>
</body>
</html>
""".trimIndent())

    return builder.toString()
}

fun parseHtmlToComponents(html: String): List<WebComponent> {
    val list = mutableListOf<WebComponent>()
    
    // We search for elements that contain 'data-ws-type="[TYPE]"'
    // Regex matches the start tag and captures both the tag name, all the attributes (group 2), and the content if any (group 4).
    val compRegex = """<([a-zA-Z0-9]+)\s+([^>]*data-ws-type="([^"]+)"[^>]*)>(.*?)<\/\1>""".toRegex(RegexOption.DOT_MATCHES_ALL)
    
    val matches = compRegex.findAll(html)
    for (match in matches) {
        val attrString = match.groupValues[2]
        val typeString = match.groupValues[3]
        val innerHtml = match.groupValues[4]
        
        val type = try {
            ComponentType.valueOf(typeString.trim())
        } catch (e: Exception) {
            continue
        }
        
        val parsedProps = parseAttributes(attrString)
        val defaultProps = WebComponent.createDefault(type).properties.toMutableMap()
        
        // Merge parsed properties into default properties to preserve full schema
        for ((key, value) in parsedProps) {
            defaultProps[key] = value
        }
        
        // Attempt to fall back or enrich properties by checking inner elements if standard attributes were modified directly in HTML
        enrichFromInnerHtml(type, defaultProps, innerHtml)
        
        // Extract ID from custom id attribute if present (id="comp-XXX")
        val idRegex = """id="comp-([^"]+)"""".toRegex()
        val customId = idRegex.find(attrString)?.groupValues?.get(1) ?: UUID.randomUUID().toString()
        
        list.add(WebComponent(id = customId, type = type, properties = defaultProps))
    }
    
    return list
}

private fun parseAttributes(attrString: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    // Matches data-ws-key="value"
    val attrRegex = """data-ws-([a-zA-Z0-9\-]+)="([^"]*)"""".toRegex()
    attrRegex.findAll(attrString).forEach { match ->
        val key = match.groupValues[1].replace("-", "_")
        val value = match.groupValues[2]
        map[key] = value
    }
    return map
}

private fun enrichFromInnerHtml(type: ComponentType, props: MutableMap<String, String>, innerHtml: String) {
    try {
        when (type) {
            ComponentType.NAVBAR -> {
                // If the user manually edited text in <span> tags or link texts inside <nav>
                val spans = """<span>(.*?)</span>""".toRegex().findAll(innerHtml).toList()
                if (spans.isNotEmpty()) {
                    props["title"] = spans.last().groupValues[1].trim()
                    if (spans.size > 1) {
                        props["logo"] = spans.first().groupValues[1].trim()
                    }
                }
                val links = """<a[^>]*>(.*?)</a>""".toRegex().findAll(innerHtml).toList()
                if (links.size >= 3) {
                    props["link1"] = links[0].groupValues[1].trim()
                    props["link2"] = links[1].groupValues[1].trim()
                    props["link3"] = links[2].groupValues[1].trim()
                }
            }
            ComponentType.HERO -> {
                val h1 = """<h1[^>]*>(.*?)</h1>""".toRegex().find(innerHtml)
                if (h1 != null) {
                    props["title"] = h1.groupValues[1].trim()
                }
                val p = """<p[^>]*>(.*?)</p>""".toRegex().find(innerHtml)
                if (p != null) {
                    props["subtitle"] = p.groupValues[1].trim()
                }
                val img = """<img[^>]*src="([^"]+)"""".toRegex().find(innerHtml)
                if (img != null) {
                    props["image_url"] = img.groupValues[1].trim()
                }
                val a = """<a[^>]*href="([^"]+)"[^>]*>(.*?)</a>""".toRegex().find(innerHtml)
                if (a != null) {
                    props["btn_link"] = a.groupValues[1].trim()
                    props["btn_text"] = a.groupValues[2].trim()
                }
            }
            ComponentType.FEATURES -> {
                val h3s = """<h3[^>]*>(.*?)</h3>""".toRegex().findAll(innerHtml).toList()
                val ps = """<p[^>]*>(.*?)</p>""".toRegex().findAll(innerHtml).toList()
                if (h3s.size >= 3) {
                    props["title1"] = h3s[0].groupValues[1].trim()
                    props["title2"] = h3s[1].groupValues[1].trim()
                    props["title3"] = h3s[2].groupValues[1].trim()
                }
                if (ps.size >= 3) {
                    props["desc1"] = ps[0].groupValues[1].trim()
                    props["desc2"] = ps[1].groupValues[1].trim()
                    props["desc3"] = ps[2].groupValues[1].trim()
                }
            }
            ComponentType.TEXT_BLOCK -> {
                val h2 = """<h2[^>]*>(.*?)</h2>""".toRegex().find(innerHtml)
                if (h2 != null) {
                    props["title"] = h2.groupValues[1].trim()
                }
                val p = """<p[^>]*>(.*?)</p>""".toRegex().find(innerHtml)
                if (p != null) {
                    props["content"] = p.groupValues[1].trim()
                }
            }
            ComponentType.IMAGE_BLOCK -> {
                val img = """<img[^>]*src="([^"]+)"""".toRegex().find(innerHtml)
                if (img != null) {
                    props["image_url"] = img.groupValues[1].trim()
                }
                val p = """<p[^>]*>(.*?)</p>""".toRegex().find(innerHtml)
                if (p != null) {
                    props["caption"] = p.groupValues[1].trim()
                }
            }
            ComponentType.BUTTON_LINK -> {
                val a = """<a[^>]*href="([^"]+)"[^>]*>(.*?)</a>""".toRegex().find(innerHtml)
                if (a != null) {
                    props["url"] = a.groupValues[1].trim()
                    props["text"] = a.groupValues[2].trim()
                }
            }
            ComponentType.CARD_WIDGET -> {
                val img = """<img[^>]*src="([^"]+)"""".toRegex().find(innerHtml)
                if (img != null) {
                    props["image_url"] = img.groupValues[1].trim()
                }
                val h3 = """<h3[^>]*>(.*?)</h3>""".toRegex().find(innerHtml)
                if (h3 != null) {
                    props["title"] = h3.groupValues[1].trim()
                }
                val span = """<span[^>]*>(.*?)</span>""".toRegex().find(innerHtml)
                if (span != null) {
                    props["price"] = span.groupValues[1].trim()
                }
                val p = """<p[^>]*>(.*?)</p>""".toRegex().find(innerHtml)
                if (p != null) {
                    props["desc"] = p.groupValues[1].trim()
                }
                val btn = """<button[^>]*>(.*?)</button>""".toRegex().find(innerHtml)
                if (btn != null) {
                    props["btn_text"] = btn.groupValues[1].trim()
                }
            }
            ComponentType.CONTACT_FORM -> {
                val h3 = """<h3[^>]*>(.*?)</h3>""".toRegex().find(innerHtml)
                if (h3 != null) {
                    props["title"] = h3.groupValues[1].trim()
                }
                val p = """<p[^>]*>(.*?)</p>""".toRegex().find(innerHtml)
                if (p != null) {
                    props["desc"] = p.groupValues[1].trim()
                }
                val btn = """<button[^>]*>(.*?)</button>""".toRegex().find(innerHtml)
                if (btn != null) {
                    props["btn_text"] = btn.groupValues[1].trim()
                }
            }
            ComponentType.FOOTER -> {
                val p = """<p[^>]*>(.*?)</p>""".toRegex().find(innerHtml)
                if (p != null) {
                    props["text"] = p.groupValues[1].trim()
                }
                val links = """<a[^>]*>(.*?)</a>""".toRegex().findAll(innerHtml).toList()
                if (links.size >= 2) {
                    props["link1"] = links[0].groupValues[1].trim()
                    props["link2"] = links[1].groupValues[1].trim()
                }
            }
        }
    } catch (e: Exception) {
        // Fallback to defaults or parse what is available
    }
}
