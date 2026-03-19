<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal - Ошибка</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            min-height: 100vh;
            padding: 20px;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .error-container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            padding: 50px 40px;
            text-align: center;
            max-width: 600px;
            width: 100%;
            animation: fadeInUp 0.8s ease-out;
        }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .error-icon {
            font-size: 4rem;
            margin-bottom: 20px;
        }

        .error-title {
            font-size: 2.5rem;
            font-weight: 800;
            background: linear-gradient(135deg, #667eea, #764ba2);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 15px;
        }

        .error-message {
            color: #666;
            font-size: 1.2rem;
            margin-bottom: 30px;
            line-height: 1.5;
        }

        .error-details {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 15px;
            margin: 20px 0;
            text-align: left;
            font-family: monospace;
            font-size: 0.9rem;
            color: #dc3545;
            border-left: 4px solid #dc3545;
        }

        .button-group {
            display: flex;
            flex-direction: column;
            gap: 15px;
            margin-top: 30px;
        }

        .btn {
            padding: 15px 25px;
            border: none;
            border-radius: 12px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-block;
            text-align: center;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
        }

        .btn-secondary {
            background: transparent;
            color: #667eea;
            border: 2px solid #667eea;
        }

        .btn-secondary:hover {
            background: #667eea;
            color: white;
            transform: translateY(-2px);
        }

        /* Стили для игры */
        .game-container {
            margin: 30px 0;
            border: 2px solid #e9ecef;
            border-radius: 10px;
            overflow: hidden;
            background: #f8f9fa;
        }

        .game-info {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 15px;
            background: #e9ecef;
            border-bottom: 1px solid #dee2e6;
        }

        .game-stats {
            display: flex;
            gap: 20px;
            font-weight: bold;
            color: #495057;
        }

        .game-stat {
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .stat-value {
            font-size: 1.1rem;
            font-weight: 800;
        }

        .stat-label {
            font-size: 0.7rem;
            color: #6c757d;
        }

        .game-controls {
            display: flex;
            gap: 10px;
        }

        .game-btn {
            padding: 5px 15px;
            border: none;
            border-radius: 5px;
            background: #667eea;
            color: white;
            cursor: pointer;
            font-size: 0.8rem;
            transition: background 0.3s;
        }

        .game-btn:hover {
            background: #5a6fd8;
        }

        .game-canvas {
            width: 100%;
            height: 150px;
            background: white;
            display: block;
            cursor: pointer;
        }

        .game-instructions {
            font-size: 0.8rem;
            color: #6c757d;
            margin-top: 10px;
            line-height: 1.4;
        }

        .speed-indicator {
            color: #e67e22;
            font-weight: bold;
        }

        @media (max-width: 480px) {
            .error-container {
                padding: 30px 20px;
                margin: 20px;
            }

            .error-title {
                font-size: 2rem;
            }

            .btn {
                padding: 12px 20px;
                font-size: 0.95rem;
            }

            .game-canvas {
                height: 120px;
            }

            .game-stats {
                gap: 10px;
            }

            .stat-value {
                font-size: 1rem;
            }
        }
    </style>
</head>
<body>
<div class="error-container">
    <div class="error-title">Окак...</div>

    <div class="error-message">
        <%
            String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null) {
                out.print(errorMessage);
            } else {
                out.print("Произошла непредвиденная ошибка");
            }
        %>
    </div>

    <% if (exception != null) { %>
    <div class="error-details">
        <strong>Тип ошибки:</strong> <%= exception.getClass().getSimpleName() %><br>
        <strong>Сообщение:</strong> <%= exception.getMessage() %>
    </div>
    <% } %>

    <!-- Игра в динозаврика -->
    <div class="game-container">
        <div class="game-info">
            <div class="game-stats">
                <div class="game-stat">
                    <div class="stat-value" id="cactiPassed">0</div>
                    <div class="stat-label">КАКТУСЫ</div>
                </div>
                <div class="game-stat">
                    <div class="stat-value" id="speed">3.0</div>
                    <div class="stat-label">СКОРОСТЬ</div>
                </div>
            </div>
            <div class="game-controls">
                <button class="game-btn" id="startBtn">Играть</button>
                <button class="game-btn" id="resetBtn">Сброс</button>
            </div>
        </div>
        <canvas id="gameCanvas" class="game-canvas" width="560" height="150"></canvas>
        <div class="game-instructions">
            Пробел/↑/клик - прыжок <span class="speed-indicator"></span>
        </div>
    </div>

    <div class="button-group">
        <a href="home.jsp" class="btn btn-primary">На главную</a>
        <a href="javascript:history.back()" class="btn btn-secondary">Назад</a>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const canvas = document.getElementById('gameCanvas');
        const ctx = canvas.getContext('2d');
        const cactiPassedElement = document.getElementById('cactiPassed');
        const speedElement = document.getElementById('speed');
        const startBtn = document.getElementById('startBtn');
        const resetBtn = document.getElementById('resetBtn');

        // Настройки игры
        const dino = {
            x: 50,
            y: 0,
            width: 30,
            height: 30,
            dy: 0,
            jumpPower: 12,
            grounded: true,
            color: '#FF5F1F'
        };

        const ground = {
            y: canvas.height - 20,
            height: 20,
            color: '#9ACD32'
        };

        const gravity = 0.8;
        let obstacles = [];
        let cactiPassed = 0;
        let gameSpeed = 2.0;
        let baseSpeed = 3.0;
        let speedIncreaseRate = 0.0005;
        let gameRunning = false;
        let animationId = null;
        let lastObstacleTime = 0;
        let minObstacleInterval = 1500;
        let maxObstacleInterval = 3000;
        let lastUpdateTime = 0;

        // Установка начальной позиции динозавра
        dino.y = ground.y - dino.height;

        // Обработчики кнопок
        startBtn.addEventListener('click', startGame);
        resetBtn.addEventListener('click', resetGame);

        // Управление клавишами
        document.addEventListener('keydown', function(e) {
            if ((e.code === 'Space' || e.code === 'ArrowUp') && dino.grounded && gameRunning) {
                performJump();
            }
        });

        // Клик для прыжка
        canvas.addEventListener('click', function(e) {
            if (dino.grounded && gameRunning) {
                performJump();
            }
        });

        function performJump() {
            dino.dy = -dino.jumpPower;
            dino.grounded = false;
        }

        function startGame() {
            if (!gameRunning) {
                gameRunning = true;
                cactiPassed = 0;
                gameSpeed = baseSpeed;
                obstacles = [];
                lastObstacleTime = performance.now();
                lastUpdateTime = performance.now();
                updateStats();

                // Останавливаем предыдущую анимацию если была
                if (animationId) {
                    cancelAnimationFrame(animationId);
                }
                gameLoop();
            }
        }

        function resetGame() {
            gameRunning = false;
            cactiPassed = 0;
            gameSpeed = baseSpeed;
            obstacles = [];
            dino.y = ground.y - dino.height;
            dino.dy = 0;
            dino.grounded = true;
            updateStats();
            draw();

            if (animationId) {
                cancelAnimationFrame(animationId);
                animationId = null;
            }
        }

        function updateStats() {
            cactiPassedElement.textContent = cactiPassed;
            speedElement.textContent = gameSpeed.toFixed(1);
        }

        function increaseSpeed() {
            gameSpeed += speedIncreaseRate;

            if (gameSpeed > 12) {
                gameSpeed = 12;
            }
        }

        function draw() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);

            ctx.fillStyle = ground.color;
            ctx.fillRect(0, ground.y, canvas.width, ground.height);

            // Рисование динозавра
            ctx.fillStyle = dino.color;
            ctx.fillRect(dino.x, dino.y, dino.width, dino.height);

            // Глаз динозавра
            ctx.fillStyle = 'white';
            ctx.fillRect(dino.x + 20, dino.y + 10, 6, 6);

            // Рисование зеленых кактусов
            obstacles.forEach(obstacle => {
                // Основной стебель кактуса - зеленый
                ctx.fillStyle = '#27ae60';
                ctx.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);

                // Добавляем детали кактусам - более темный зеленый
                ctx.fillStyle = '#219a52';
                ctx.fillRect(obstacle.x - 3, obstacle.y - 5, obstacle.width + 6, 5);
                ctx.fillRect(obstacle.x - 5, obstacle.y + 10, 5, 10);
                ctx.fillRect(obstacle.x + obstacle.width, obstacle.y + 15, 5, 8);

                // Колючки - светло-зеленые
                ctx.fillStyle = '#2ecc71';
                ctx.fillRect(obstacle.x + 2, obstacle.y + 5, 2, 8);
                ctx.fillRect(obstacle.x + obstacle.width - 4, obstacle.y + 8, 2, 6);
            });

            if (!gameRunning && cactiPassed > 0) {
                ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
                ctx.fillRect(0, 0, canvas.width, canvas.height);

                ctx.fillStyle = 'white';
                ctx.font = '20px Arial';
                ctx.textAlign = 'center';
                ctx.fillText('Игра окончена!', canvas.width / 2, canvas.height / 2 - 15);
                ctx.font = '16px Arial';
                ctx.fillText('Кактусы: ' + cactiPassed, canvas.width / 2, canvas.height / 2 + 30);
            }
        }

        function update(currentTime) {
            if (!gameRunning) return;

            const deltaTime = currentTime - lastUpdateTime;
            lastUpdateTime = currentTime;

            // Постепенное увеличение скорости
            increaseSpeed();
            updateStats();

            // Обновление динозавра
            dino.y += dino.dy;
            dino.dy += gravity;

            // Проверка земли
            if (dino.y >= ground.y - dino.height) {
                dino.y = ground.y - dino.height;
                dino.dy = 0;
                dino.grounded = true;
            }

            // Генерация препятствий (интервал зависит от скорости)
            const speedFactor = Math.max(0.5, 1 - (gameSpeed - baseSpeed) * 0.1);
            const currentMinInterval = minObstacleInterval * speedFactor;
            const currentMaxInterval = maxObstacleInterval * speedFactor;

            if (currentTime - lastObstacleTime > getRandomInterval(currentMinInterval, currentMaxInterval)) {
                generateObstacle();
                lastObstacleTime = currentTime;
            }

            // Обновление препятствий
            for (let i = obstacles.length - 1; i >= 0; i--) {
                obstacles[i].x -= gameSpeed;

                // Проверка пройденных кактусов
                if (!obstacles[i].passed && obstacles[i].x + obstacles[i].width < dino.x) {
                    obstacles[i].passed = true;
                    cactiPassed++;
                }

                // Удаление вышедших за пределы препятствий
                if (obstacles[i].x + obstacles[i].width < 0) {
                    obstacles.splice(i, 1);
                    continue; // Пропускаем проверку столкновений для удаленного кактуса
                }

                // Проверка столкновений
                if (checkCollision(dino, obstacles[i])) {
                    gameRunning = false;
                    return;
                }
            }
        }

        function getRandomInterval(min, max) {
            return min + Math.random() * (max - min);
        }

        function generateObstacle() {
            const types = [
                { width: 15, height: 25 + Math.random() * 15 },
                { width: 18, height: 30 + Math.random() * 20 },
                { width: 22, height: 35 + Math.random() * 25 }
            ];

            const type = types[Math.floor(Math.random() * types.length)];
            const minDistance = 250;
            const maxDistance = 450;

            // Проверяем расстояние до последнего кактуса
            if (obstacles.length > 0) {
                const lastObstacle = obstacles[obstacles.length - 1];
                const distanceToLast = lastObstacle.x - (canvas.width + minDistance);
                if (distanceToLast > 0) {
                    return; // Ждем пока последний кактус уедет достаточно далеко
                }
            }

            obstacles.push({
                x: canvas.width,
                y: ground.y - type.height,
                width: type.width,
                height: type.height,
                passed: false
            });
        }

        function checkCollision(rect1, rect2) {
            return (
                rect1.x < rect2.x + rect2.width &&
                rect1.x + rect1.width > rect2.x &&
                rect1.y < rect2.y + rect2.height &&
                rect1.y + rect1.height > rect2.y
            );
        }

        function gameLoop(currentTime) {
            if (!gameRunning) return;

            try {
                update(currentTime);
                draw();

                if (gameRunning) {
                    animationId = requestAnimationFrame(gameLoop);
                }
            } catch (error) {
                console.error('Game error:', error);
                gameRunning = false;
            }
        }

        // Начальная отрисовка
        draw();
    });
</script>
</body>
</html>