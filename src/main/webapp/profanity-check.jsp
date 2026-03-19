<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.announcementContent.ProfanityChecker" %>

<%!
    // Объявление метода с использованием декларации JSP
    boolean checkProfanity(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        ProfanityChecker profanityChecker = new ProfanityChecker();
        return profanityChecker.containsProfanity(text);
    }
%>

<script>
    function checkProfanityAsync(text, callback) {
        if (!text || text.trim() === '') {
            callback(false);
            return;
        }

        // Create form data
        const formData = new FormData();
        formData.append('text', text);
        formData.append('action', 'checkProfanity');

        // Make AJAX request
        fetch('profanity-check-handler.jsp', {
            method: 'POST',
            body: formData
        })
            .then(response => response.text())
            .then(result => {
                const hasProfanity = result.trim() === 'true';
                callback(hasProfanity);
            })
            .catch(error => {
                console.error('Error checking profanity:', error);
                callback(false);
            });
    }

    function showProfanityWarning(formId, fieldNames) {

        if (formId && fieldNames) {
            const form = document.getElementById(formId);
            if (form) {
                fieldNames.forEach(fieldName => {
                    const field = form.querySelector('[name="' + fieldName + '"]');
                    if (field) {
                        field.value = '';
                    }
                });
            }
        }

        const overlay = document.createElement('div');
        overlay.id = 'profanity-overlay';
        overlay.style.cssText = 'position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 10000; display: flex; align-items: center; justify-content: center;';

        const modal = document.createElement('div');
        modal.style.cssText = 'background: white; padding: 30px; border-radius: 15px; max-width: 500px; box-shadow: 0 10px 40px rgba(0,0,0,0.3); text-align: center;';
        modal.innerHTML = `
            <div style="font-size: 4rem; margin-bottom: 20px;">⚠️</div>
            <h2 style="color: #f72585; margin-bottom: 15px; font-size: 1.5rem;">Обнаружена ненормативная лексика</h2>
            <p style="color: #666; margin-bottom: 25px; line-height: 1.6;">
                Ваше сообщение содержит недопустимые слова. Пожалуйста, переформулируйте текст и попробуйте снова.
            </p>
            <button onclick="closeProfanityWarning()" style="background: linear-gradient(135deg, #4361ee, #7209b7); color: white; border: none; padding: 12px 30px; border-radius: 10px; font-size: 1rem; font-weight: 600; cursor: pointer; transition: all 0.3s ease;">
                Понятно
            </button>
        `;

        overlay.appendChild(modal);
        document.body.appendChild(overlay);

        setTimeout(() => {
            overlay.style.opacity = '1';
            modal.style.transform = 'scale(1)';
        }, 10);
    }

    function closeProfanityWarning() {
        const overlay = document.getElementById('profanity-overlay');
        if (overlay) {
            overlay.style.opacity = '0';
            setTimeout(() => {
                overlay.remove();
            }, 300);
        }
    }

    function validateFormWithProfanity(formId, textFields, callback) {
        const form = document.getElementById(formId);
        if (!form) {
            console.error('Form not found:', formId);
            return;
        }

        let allText = '';
        textFields.forEach(fieldName => {
            const field = form.querySelector('[name="' + fieldName + '"]');
            if (field && field.value) {
                allText += field.value + ' ';
            }
        });

        if (allText.trim() === '') {
            callback();
            return;
        }

        checkProfanityAsync(allText.trim(), function(hasProfanity) {
            if (hasProfanity) {
                textFields.forEach(fieldName => {
                    const field = form.querySelector('[name="' + fieldName + '"]');
                    if (field) {
                        field.value = '';
                    }
                });
                showProfanityWarning(formId, textFields);
            } else {
                callback();
            }
        });
    }

    // Универсальная функция для проверки формы объявления
    function validateAdForm(formId, fieldNames, callback) {
        const form = document.getElementById(formId);
        if (!form) {
            console.error('Form not found:', formId);
            return;
        }

        let allText = '';
        fieldNames.forEach(fieldName => {
            const field = form.querySelector('[name="' + fieldName + '"]');
            if (field && field.value) {
                allText += field.value + ' ';
            }
        });

        if (allText.trim() === '') {
            callback();
            return;
        }

        checkProfanityAsync(allText.trim(), function(hasProfanity) {
            if (hasProfanity) {
                showProfanityWarning(formId, fieldNames);
            } else {
                callback();
            }
        });
    }
</script>
<style>
    #profanity-overlay {
        animation: fadeIn 0.3s ease;
    }

    #profanity-overlay > div {
        animation: scaleIn 0.3s ease;
    }

    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }

    @keyframes scaleIn {
        from { transform: scale(0.9); opacity: 0; }
        to { transform: scale(1); opacity: 1; }
    }
</style>