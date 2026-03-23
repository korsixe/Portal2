import telebot
import re
import psycopg2

bot = telebot.TeleBot('8441149825:AAHWjnuLsOk2AZhctVFrppMgJvPWLD3J65I')

user_states = {}
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'myproject',
    'user': 'myuser',
    'password': 'mypassword'
}


def is_valid_email(email):
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return re.match(pattern, email) is not None


@bot.message_handler(commands=['start'])
def send_welcome(message):
    bot.send_message(message.chat.id,
                     "Привет! Я бот-помощник сайта Portal. \nЧтобы получать уведомления, введите свой phystech.edu адрес, с которым ты авторизован на сайте:")
    user_states[message.chat.id] = 'waiting_email'


@bot.message_handler(func=lambda message: True)
def handle_message(message):
    chat_id = message.chat.id
    if chat_id in user_states and user_states[chat_id] == 'waiting_email':
        email = message.text.strip()
        if is_valid_email(email):
            if email.endswith('@phystech.edu'):
                bot.send_message(chat_id, f"✅ Email {email} успешно сохранен!")
                with open('users.txt', 'a', encoding='utf-8') as f:
                    f.write(f"{chat_id}:{email}\n")

                bot.send_message(chat_id,
                                 f"Готово! \nТеперь уведомления от пользователя {email} будут приходить в этот чат. \nИзменить email: /start")
                user_states[chat_id] = 'registered'
                #added_email(chat_id, email)
            else:
                bot.send_message(chat_id, "Неверный формат email.")
        else:
            bot.send_message(chat_id, "Неверный формат email.")


bot.infinity_polling()
