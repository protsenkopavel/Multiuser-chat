# Multi-user chat application

Данное приложение является многомодульным чат-сервером, разработанным в рамках дипломного проекта Java Sber school.
Приложение предоставляет возможность обмена текстовыми сообщениями между пользователями через сеть по протоколам TCP/IP.

Для реализации сетевого взаимодействия принят класс java.net.Socket, позволяющий создавать конечные точки (сокекты)
для связи между несколькими машинами в локальной сети, по указанному ip-адрессу и порту.
Входящий/исходящий поток представлен потоком байтов, преобразованным в JSON для повышения функцианальности взаимодействия. 

## Структура проекта

1. Серверная часть - "слушающий сокет", получает запросы по указанному адресу.

При подключении клиента сервер создает новый поток для каждого клиента и начинает прослушивать его сообщения.
Обработчик запросов, в зависимости от типа запроса и его валидности, определяет состояние пользователя (не существует,
оффлайн, онлайн) и формирует исходящее событие, при необходимости манипулируя базой данных. Запросы добавляются в очередь
и обрабатываются асинхронно.
Хранение данных о пользователе в базе данных осуществляется в закодированном состоянии при помощи алгоритма кодирования
base64.

Диспетчер сообщений обрабатывает сформированные события, включащие в себя ответ, соответствующий ему поток вывода и статус
ответа, на основании чего диспетчер передает ответ.

На всем этапе работы, в отдельном потоке менеджер управления жизненным циклом соединений контролирует валидность 
взаимодействия, и при наличии блокирующих факторов, ограничивает прием новых сообщений на этапе обработки в данном
соединении.

2. Клиентская часть - инициирует соединение с сервером, отправляет запросы и получает ответы в формате JSON.
Пользователь взаимодействует с клиентом через консольный интерфейс, вводя команды и текст сообщений.
Клиент формирует запрос, содержащий в себе тип запроса: регистрация, вход в систему, выход из системы, запрос количества
онлайн пользователей, отправка сообщения. Также запрос включает в себя информацию для входа/аутентификации (username/
password) и само сообщение.



## Исходное техническое задание

Описание задачи:
1. Реализовать сервер, который:
- регистрирует пользователя;
- передает список on-line пользователей клиенту и поддерживает его в активном состоянии;
- фиксирует время передаваемых сообщений, ведет log журнал;
- мониторит статус пользователей;

2. Реализовать клиент, который:
- отправляет запрос об регистрации пользователя на сервер;
- получает список on-line пользователей с сервера и обновляет его в реальном времени;
- передает и отправляет сообщение другому клиенту;

3. Рекомендации по реализации:
- Использовать Java Socket для реализации сетевого взаимодействия. 
- Для хранения данных, в том числе о пользователях и истории сообщений, на сервере использовать
любую in memory БД, например H2. 
- Доступ к БД осуществлять через JDBC (по желанию ORM).
- На клиентской части разделить между разными потоками задачи мониторинга входящих сообщений,
мониторинга списка online пользователей, отправки сообщений
- На серверной части разделить между потоками обработку событий от каждого пользователя (вход/выход/отправка сообщений)

4. Нефункциональные требования:
- Плановый срок реализации от 14 до 21 дня;
- В состав проекта необходимо включить файл с документацией (описание классов\методов и алгоритма работы);
- Результат необходимо сохранить в своем репозитории на github и прислать ссылку руководителям практики