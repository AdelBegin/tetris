const canvas = document.getElementById('tetris');
const context = canvas.getContext('2d');
const ROWS = 20;
const COLUMNS = 10;
canvas.width = COLUMNS * 30;
canvas.height = ROWS * 30;

const colors = [null, 'cyan', 'blue', 'orange', 'yellow', 'green', 'purple', 'red'];
const pieces = 'IJLOSTZ';
let board = Array.from({ length: ROWS }, () => Array(COLUMNS).fill(0));
let currentPiece = createPiece();
let dropInterval = 1000;
let lastTime = 0;
let fastDrop = false;
let score = 0;

function createPiece() {
    const type = pieces[Math.floor(Math.random() * pieces.length)];
    return {
        type,
        color: getRandomColor(),
        x: 4,
        y: 0,
        rotation: 0
    };
}

function getRandomColor() {
    const randomIndex = Math.floor(Math.random() * (colors.length - 1)) + 1;
    return colors[randomIndex];
}

function draw() {
    context.clearRect(0, 0, canvas.width, canvas.height);
    drawBoard();
    drawPiece(currentPiece);
}

function drawBoard() {
    board.forEach((row, y) => {
        row.forEach((value, x) => {
            if (value) {
                context.fillStyle = colors[value];
                context.fillRect(x * 30, y * 30, 30, 30);
                context.strokeStyle = '#000';
                context.strokeRect(x * 30, y * 30, 30, 30);
            }
        });
    });
}

function drawPiece(piece) {
    const shape = getPieceShape(piece);
    shape.forEach((row, y) => {
        row.forEach((value, x) => {
            if (value) {
                context.fillStyle = piece.color;
                context.fillRect((piece.x + x) * 30, (piece.y + y) * 30, 30, 30);
                context.strokeStyle = '#000';
                context.strokeRect((piece.x + x) * 30, (piece.y + y) * 30, 30, 30);
            }
        });
    });
}

function getPieceShape(piece) {
    const shapes = {
        'I': [[[1, 1, 1, 1]], [[1], [1], [1], [1]]],
        'J': [[[0, 0, 1], [1, 1, 1]], [[1, 0], [1, 0], [1, 1]], [[1, 1, 1], [0, 0, 1]], [[1, 1], [0, 1], [0, 1]]],
        'L': [[[1, 0, 0], [1, 1, 1]], [[0, 1], [0, 1], [1, 1]], [[1, 1, 1], [0, 0, 1]], [[1, 1], [1, 0], [0, 1]]],
        'O': [[[1, 1], [1, 1]]],
        'S': [[[0, 1, 1], [1, 1, 0]], [[1, 0], [1, 1], [0, 1]]],
        'T': [[[0, 1, 0], [1, 1, 1]], [[1, 0], [1, 1], [1, 0]], [[1, 1, 1], [0, 1, 0]], [[0, 1], [1, 1], [0, 1]]],
        'Z': [[[1, 1, 0], [0, 1, 1]], [[0, 1], [1, 1], [1, 0]]]
    };
    return shapes[piece.type][piece.rotation % shapes[piece.type].length];
}

function collide(xOffset = 0, yOffset = 0) {
    const shape = getPieceShape(currentPiece);
    for (let y = 0; y < shape.length; y++) {
        for (let x = 0; x < shape[y].length; x++) {
            if (shape[y][x] && (board[y + currentPiece.y + yOffset] && board[y + currentPiece.y + yOffset][x + currentPiece.x + xOffset]) !== 0) {
                return true;
            }
        }
    }
    return false;
}

function merge() {
    const shape = getPieceShape(currentPiece);
    shape.forEach((row, y) => {
        row.forEach((value, x) => {
            if (value) {
                board[y + currentPiece.y][x + currentPiece.x] = colors.indexOf(currentPiece.color);
            }
        });
    });
}

function clearLines() {
    let linesCleared = 0;
    board = board.reduce((acc, row) => {
        if (row.every(value => value !== 0)) {
            linesCleared++;
            acc.unshift(Array(COLUMNS).fill(0));
        } else {
            acc.push(row);
        }
        return acc;
    }, []);
    
    score += linesCleared * 100; // Увеличение счёта
    updateScoreDisplay(); // Обновление счёта
}

function updateScoreDisplay() {
    document.getElementById('score').innerText = `Score: ${score}`;
}

function update() {
    currentPiece.y++;
    if (collide()) {
        currentPiece.y--;
        merge();
        clearLines();
        currentPiece = createPiece();
        if (collide()) {
            alert('Game Over!');
            board = Array.from({ length: ROWS }, () => Array(COLUMNS).fill(0));
            score = 0; // Сброс счёта
            updateScoreDisplay(); // Обновление счёта
        }
    }
}

function gameLoop(time = 0) {
    if (time - lastTime > (fastDrop ? 50 : dropInterval)) {
        update();
        lastTime = time;
    }
    draw();
    requestAnimationFrame(gameLoop);
}

// Запуск игры
gameLoop();

// Обработка сенсорных событий
let touchStartX = null;

canvas.addEventListener('touchstart', (e) => {
    touchStartX = e.touches[0].clientX;
    fastDrop = true; // Начинаем быстрое падение
});

canvas.addEventListener('touchend', () => {
    fastDrop = false; // Останавливаем быстрое падение
});

canvas.addEventListener('touchmove', (e) => {
    if (touchStartX === null) return;

    const touchEndX = e.touches[0].clientX;
    const diffX = touchEndX - touchStartX;

    if (Math.abs(diffX) > 30) {
        if (diffX > 0) {
            currentPiece.x++;
            if (collide()) currentPiece.x--;
        } else {
            currentPiece.x--;
            if (collide()) currentPiece.x++;
        }
        touchStartX = touchEndX; // Обновляем позицию для следующего движения
    }
});

// Обработка двойного клика для вращения фигуры
canvas.addEventListener('dblclick', () => {
    currentPiece.rotation++;
        if (collide()) {
        currentPiece.rotation--; // Если вращение невозможно, возвращаем назад
    }
});

// Обработка клавиатуры
document.addEventListener('keydown', (event) => {
    switch (event.key) {
        case 'ArrowLeft':
            currentPiece.x--;
            if (collide()) currentPiece.x++;
            break;
        case 'ArrowRight':
            currentPiece.x++;
            if (collide()) currentPiece.x--;
            break;
        case 'ArrowDown':
            currentPiece.y++;
            if (collide()) {
                currentPiece.y--;
                merge();
                clearLines();
                currentPiece = createPiece();
                if (collide()) {
                    alert('Game Over!');
                    board = Array.from({ length: ROWS }, () => Array(COLUMNS).fill(0));
                    score = 0; // Сброс счёта
                    updateScoreDisplay(); // Обновление счёта
                }
            }
            break;
        case 'ArrowUp':
            currentPiece.rotation++;
            if (collide()) {
                currentPiece.rotation--; // Если вращение невозможно, возвращаем назад
            }
            break;
    }
});

