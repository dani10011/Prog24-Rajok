package com.example.classmasterpro.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classmasterpro.ui.theme.*
import kotlinx.coroutines.delay

// ============================================================================
// DATA MODELS
// ============================================================================

enum class Suit(val symbol: String, val color: Color) {
    HEARTS("♥", Color.Red),
    DIAMONDS("♦", Color.Red),
    CLUBS("♣", Color.Black),
    SPADES("♠", Color.Black)
}

enum class Rank(val displayName: String, val value: Int) {
    ACE("A", 11),
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 10),
    QUEEN("Q", 10),
    KING("K", 10)
}

data class Card(val suit: Suit, val rank: Rank, val id: Int = 0)

enum class DealerMood {
    IDLE,
    DEALING,
    HAPPY,
    SAD,
    THINKING
}

data class BlackjackGameState(
    val deck: List<Card>,
    val playerHand: List<Card>,
    val dealerHand: List<Card>,
    val playerBalance: Int,
    val currentBet: Int,
    val gamePhase: GamePhase,
    val dealerRevealed: Boolean = false,
    val resultMessage: String = "",
    val dealerMood: DealerMood = DealerMood.IDLE,
    val showChipAnimation: Boolean = false
)

enum class GamePhase {
    BETTING,
    PLAYING,
    DEALER_TURN,
    GAME_OVER
}

// ============================================================================
// MAIN SCREEN
// ============================================================================

@Composable
fun BlackjackScreen() {
    var gameState by remember { mutableStateOf(createInitialGameState()) }
    var betInput by remember { mutableStateOf("") }

    // Stunning casino background with rich gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Deep royal blue
                        Color(0xFF283593), // Rich blue
                        Color(0xFF1B5E20), // Emerald green
                        Color(0xFF2E7D32)  // Forest green
                    )
                )
            )
    ) {
        // Premium geometric pattern overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Diamond pattern for luxury feel
            val spacing = 120f
            val patternAlpha = 0.08f

            for (i in 0..20) {
                for (j in 0..30) {
                    val x = i * spacing + (if (j % 2 == 0) spacing / 2 else 0f)
                    val y = j * spacing / 2

                    // Diamond shapes
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = patternAlpha),
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }

            // Elegant corner accents
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(0f, 0f),
                    radius = 300f
                ),
                radius = 300f,
                center = Offset(0f, 0f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(size.width, size.height),
                    radius = 300f
                ),
                radius = 300f,
                center = Offset(size.width, size.height)
            )
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Premium Title
            Text(
                text = "♠ BLACKJACK ♥",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700),
                    letterSpacing = 3.sp,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        offset = Offset(0f, 4f),
                        blurRadius = 12f
                    )
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Balance and Bet Info
            BalanceCard(
                balance = gameState.playerBalance,
                currentBet = gameState.currentBet,
                gamePhase = gameState.gamePhase
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Dealer's Hand with Animated Dealer
            DealerHandSection(
                hand = gameState.dealerHand,
                revealed = gameState.dealerRevealed,
                gamePhase = gameState.gamePhase,
                dealerMood = gameState.dealerMood
            )

            Spacer(modifier = Modifier.weight(0.8f))

            // Animated Chips in center
            AnimatedChips(
                visible = gameState.showChipAnimation,
                betAmount = gameState.currentBet
            )

            // Result Message
            AnimatedVisibility(
                visible = gameState.resultMessage.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                ResultMessageCard(message = gameState.resultMessage)
            }

            Spacer(modifier = Modifier.weight(0.8f))

            // Player's Hand
            PlayerHandSection(
                hand = gameState.playerHand,
                gamePhase = gameState.gamePhase
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Controls
            when (gameState.gamePhase) {
                GamePhase.BETTING -> {
                    BettingControls(
                        balance = gameState.playerBalance,
                        betInput = betInput,
                        onBetInputChange = { betInput = it },
                        onPlaceBet = { amount ->
                            if (amount > 0 && amount <= gameState.playerBalance) {
                                gameState = startNewRound(gameState, amount)
                                betInput = ""
                            }
                        },
                        onQuickBet = { amount ->
                            if (amount <= gameState.playerBalance) {
                                gameState = startNewRound(gameState, amount)
                            }
                        }
                    )
                }
                GamePhase.PLAYING -> {
                    GameControls(
                        onHit = {
                            gameState = playerHit(gameState)
                        },
                        onStand = {
                            gameState = playerStand(gameState)
                            // Dealer plays
                            gameState = playDealerTurn(gameState)
                        },
                        onDoubleDown = {
                            if (gameState.playerHand.size == 2 &&
                                gameState.currentBet * 2 <= gameState.playerBalance) {
                                gameState = playerDoubleDown(gameState)
                            }
                        },
                        canDoubleDown = gameState.playerHand.size == 2 &&
                                       gameState.currentBet * 2 <= gameState.playerBalance
                    )
                }
                GamePhase.GAME_OVER -> {
                    GameOverControls(
                        onNewRound = {
                            if (gameState.playerBalance >= 10) {
                                gameState = createInitialGameState().copy(
                                    playerBalance = gameState.playerBalance
                                )
                            } else {
                                // Reset balance if broke
                                gameState = createInitialGameState()
                            }
                        },
                        isBroke = gameState.playerBalance < 10
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
        }
    }
}

// ============================================================================
// ANIMATED DEALER
// ============================================================================

@Composable
private fun AnimatedDealer(mood: DealerMood, gamePhase: GamePhase) {
    val infiniteTransition = rememberInfiniteTransition(label = "dealer")

    // Idle bobbing animation
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    // Blinking animation
    val blinkScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(150),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(3000)
        ),
        label = "blink"
    )

    val scale by animateFloatAsState(
        targetValue = if (mood == DealerMood.DEALING) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dealerScale"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .offset(y = bobbingOffset.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Dealer avatar circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Face background
            drawCircle(
                color = Color(0xFFFFDBAD),
                radius = size.minDimension / 2
            )

            // Eyes
            val eyeY = size.height * 0.35f
            val eyeRadius = when (mood) {
                DealerMood.HAPPY -> 8f
                DealerMood.SAD -> 6f
                else -> 7f
            }

            // Left eye
            drawCircle(
                color = Color.Black,
                radius = eyeRadius * blinkScale,
                center = Offset(size.width * 0.35f, eyeY)
            )

            // Right eye
            drawCircle(
                color = Color.Black,
                radius = eyeRadius * blinkScale,
                center = Offset(size.width * 0.65f, eyeY)
            )

            // Mouth based on mood
            val mouthY = size.height * 0.65f
            when (mood) {
                DealerMood.HAPPY -> {
                    // Happy smile (arc)
                    drawArc(
                        color = Color.Black,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(size.width * 0.3f, mouthY - 10f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.4f, 20f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )
                }
                DealerMood.SAD -> {
                    // Sad frown
                    drawArc(
                        color = Color.Black,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(size.width * 0.3f, mouthY),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.4f, 20f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )
                }
                DealerMood.THINKING -> {
                    // Straight line (thinking)
                    drawLine(
                        color = Color.Black,
                        start = Offset(size.width * 0.3f, mouthY),
                        end = Offset(size.width * 0.7f, mouthY),
                        strokeWidth = 3f
                    )
                }
                else -> {
                    // Neutral small smile
                    drawArc(
                        color = Color.Black,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(size.width * 0.35f, mouthY - 5f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.3f, 10f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }

            // Dealer hat - brown color
            drawRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(size.width * 0.2f, -5f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.6f, 10f)
            )
            drawRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(size.width * 0.3f, -15f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.4f, 15f)
            )
        }
    }
}

// ============================================================================
// ANIMATED CHIPS
// ============================================================================

@Composable
private fun AnimatedChips(visible: Boolean, betAmount: Int) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.height(60.dp)
        ) {
            // Calculate number of chips to show
            val chipCounts = calculateChipStacks(betAmount)

            chipCounts.entries.forEachIndexed { index, (denomination, count) ->
                if (count > 0) {
                    ChipStack(
                        denomination = denomination,
                        count = count.coerceAtMost(5),
                        delay = index * 100
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipStack(denomination: Int, count: Int, delay: Int) {
    val chipColor = when (denomination) {
        100 -> PrimaryBlue      // Darkest blue
        50 -> SecondaryBlue     // Mid blue
        25 -> LightBlue         // Light blue
        10 -> Accent            // Orange accent
        else -> SkyBlue         // Sky blue
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(40.dp)
            .height(60.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        repeat(count) { index ->
            val animatedOffset by animateFloatAsState(
                targetValue = -index * 4f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = 0.01f
                ),
                label = "chipStack$index"
            )

            Chip(
                color = chipColor,
                denomination = denomination,
                modifier = Modifier.offset(y = animatedOffset.dp)
            )
        }
    }
}

@Composable
private fun Chip(color: Color, denomination: Int, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chipScale"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2

            // Dramatic shadow for 3D depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = Offset(centerX + 3f, centerY + 3f),
                    radius = radius
                ),
                radius = radius,
                center = Offset(centerX + 3f, centerY + 3f)
            )

            // Main chip body - metallic gradient
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = 1f),
                        color.copy(alpha = 0.7f),
                        color.copy(alpha = 0.9f),
                        color
                    ),
                    start = Offset(centerX - radius, centerY - radius),
                    end = Offset(centerX + radius, centerY + radius)
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Brilliant highlight on top for realism
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(centerX - radius * 0.4f, centerY - radius * 0.4f),
                    radius = radius * 0.5f
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Premium outer ring - gold accent
            drawCircle(
                color = Color(0xFFFFD700),
                radius = radius - 1f,
                center = Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )

            // Secondary white ring
            drawCircle(
                color = Color.White,
                radius = radius - 6f,
                center = Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Casino-style edge decorations (12 segments)
            for (i in 0 until 12) {
                val angle = (i * 30f) * (Math.PI / 180f).toFloat()
                val innerRadius = radius - 8f
                val outerRadius = radius - 3f

                val x1 = centerX + (Math.cos(angle.toDouble()) * innerRadius).toFloat()
                val y1 = centerY + (Math.sin(angle.toDouble()) * innerRadius).toFloat()
                val x2 = centerX + (Math.cos(angle.toDouble()) * outerRadius).toFloat()
                val y2 = centerY + (Math.sin(angle.toDouble()) * outerRadius).toFloat()

                drawLine(
                    color = Color(0xFFFFD700),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // Premium denomination display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$$denomination",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

private fun calculateChipStacks(amount: Int): Map<Int, Int> {
    var remaining = amount
    val result = mutableMapOf<Int, Int>()

    listOf(100, 50, 25, 10).forEach { denom ->
        val count = remaining / denom
        if (count > 0) {
            result[denom] = count
            remaining %= denom
        }
    }

    return result
}

// ============================================================================
// UI COMPONENTS
// ============================================================================

@Composable
private fun BalanceCard(balance: Int, currentBet: Int, gamePhase: GamePhase) {
    // Glassmorphism card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.1f),
                            Color(0xFFFF6F00).copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BALANCE",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$$balance",
                            style = TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color(0xFFFFD700).copy(alpha = 0.5f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 8f
                                )
                            )
                        )
                    }
                }

                if (gamePhase != GamePhase.BETTING && currentBet > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BET",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6F00),
                                letterSpacing = 2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$$currentBet",
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF6F00),
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color(0xFFFF6F00).copy(alpha = 0.5f),
                                        offset = Offset(0f, 2f),
                                        blurRadius = 8f
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DealerHandSection(
    hand: List<Card>,
    revealed: Boolean,
    gamePhase: GamePhase,
    dealerMood: DealerMood
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F0F1E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8B0000).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Animated dealer avatar
                    AnimatedDealer(mood = dealerMood, gamePhase = gamePhase)

                    Column {
                        Text(
                            text = "DEALER",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF6B6B),
                                letterSpacing = 2.sp
                            )
                        )
                        if (revealed && hand.isNotEmpty()) {
                            Text(
                                text = "(${calculateHandValue(hand)})",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

            if (hand.isEmpty()) {
                Text(
                    text = "Waiting for bets...",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-40).dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    hand.forEachIndexed { index, card ->
                        if (index == 1 && !revealed) {
                            CardBack()
                        } else {
                            AnimatedPlayingCard(card, index)
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun PlayerHandSection(
    hand: List<Card>,
    gamePhase: GamePhase
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F0F1E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "YOU",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4CAF50),
                            letterSpacing = 2.sp
                        )
                    )
                    if (hand.isNotEmpty()) {
                        val handValue = calculateHandValue(hand)
                        Text(
                            text = "($handValue)",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (handValue > 21) Color(0xFFFF5252) else Color.White.copy(alpha = 0.9f)
                            )
                        )
                        if (handValue == 21 && hand.size == 2) {
                            Text(
                                text = "BLACKJACK!",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700),
                                    letterSpacing = 1.sp,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color(0xFFFFD700).copy(alpha = 0.5f),
                                        offset = Offset(0f, 2f),
                                        blurRadius = 8f
                                    )
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

            if (hand.isEmpty()) {
                Text(
                    text = "Place your bet to start",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-40).dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    hand.forEachIndexed { index, card ->
                        AnimatedPlayingCard(card, index)
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun AnimatedPlayingCard(card: Card, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(card) {
        delay((index * 150).toLong())
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else 180f,
        animationSpec = tween(300),
        label = "cardRotation"
    )

    Card(
        modifier = Modifier
            .width(70.dp)
            .height(100.dp)
            .scale(scale)
            .rotate(rotation),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top rank
                Text(
                    text = card.rank.displayName,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = card.suit.color
                    )
                )

                // Center suit
                Text(
                    text = card.suit.symbol,
                    style = TextStyle(
                        fontSize = 36.sp,
                        color = card.suit.color
                    )
                )

                // Bottom rank (upside down)
                Text(
                    text = card.rank.displayName,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = card.suit.color
                    ),
                    modifier = Modifier.rotate(180f)
                )
            }
        }
    }
}

@Composable
private fun CardBack() {
    Card(
        modifier = Modifier
            .width(70.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryBlue
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun ResultMessageCard(message: String) {
    val (bgColor, accentColor) = when {
        message.contains("WIN") || message.contains("BLACKJACK") -> Pair(
            Color(0xFF0F0F1E).copy(alpha = 0.95f),
            Color(0xFF4CAF50)
        )
        message.contains("LOSE") || message.contains("BUST") -> Pair(
            Color(0xFF0F0F1E).copy(alpha = 0.95f),
            Color(0xFFFF5252)
        )
        else -> Pair(
            Color(0xFF0F0F1E).copy(alpha = 0.95f),
            Color(0xFFFFD700)
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.2f),
                            accentColor.copy(alpha = 0.1f),
                            accentColor.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            Text(
                text = message,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = accentColor.copy(alpha = 0.5f),
                        offset = Offset(0f, 4f),
                        blurRadius = 12f
                    )
                ),
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 20.dp)
            )
        }
    }
}

@Composable
private fun BettingControls(
    balance: Int,
    betInput: String,
    onBetInputChange: (String) -> Unit,
    onPlaceBet: (Int) -> Unit,
    onQuickBet: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F0F1E).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PLACE YOUR BET",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700),
                        letterSpacing = 2.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

            // Quick bet buttons with chip icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(10, 25, 50, 100).forEach { amount ->
                    Button(
                        onClick = { onQuickBet(amount) },
                        enabled = amount <= balance,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (amount) {
                                100 -> PrimaryBlue
                                50 -> SecondaryBlue
                                25 -> LightBlue
                                else -> Accent
                            },
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "$$amount",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom bet input
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = betInput,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            onBetInputChange(input)
                        }
                    },
                    label = {
                        Text(
                            "Custom Bet",
                            color = Color(0xFFFFD700).copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFFFD700)
                    ),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val amount = betInput.toIntOrNull() ?: 0
                        onPlaceBet(amount)
                    },
                    enabled = betInput.isNotEmpty() &&
                             (betInput.toIntOrNull() ?: 0) <= balance &&
                             (betInput.toIntOrNull() ?: 0) > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "PLACE BET",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F0F1E),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun GameControls(
    onHit: () -> Unit,
    onStand: () -> Unit,
    onDoubleDown: () -> Unit,
    canDoubleDown: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F0F1E).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onHit,
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HIT",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            ),
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = onStand,
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "STAND",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            ),
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = onDoubleDown,
                    enabled = canDoubleDown,
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DBL",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOverControls(onNewRound: () -> Unit, isBroke: Boolean) {
    Button(
        onClick = onNewRound,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text = if (isBroke) "NEW GAME (RESET)" else "NEW ROUND",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        )
    }
}

// ============================================================================
// GAME LOGIC
// ============================================================================

private fun createInitialGameState(): BlackjackGameState {
    return BlackjackGameState(
        deck = emptyList(),
        playerHand = emptyList(),
        dealerHand = emptyList(),
        playerBalance = 1000,
        currentBet = 0,
        gamePhase = GamePhase.BETTING,
        dealerMood = DealerMood.IDLE
    )
}

private fun createShuffledDeck(): List<Card> {
    val deck = mutableListOf<Card>()
    var id = 0
    for (suit in Suit.values()) {
        for (rank in Rank.values()) {
            deck.add(Card(suit, rank, id++))
        }
    }
    return deck.shuffled()
}

private fun startNewRound(state: BlackjackGameState, bet: Int): BlackjackGameState {
    var deck = createShuffledDeck().toMutableList()

    val playerHand = mutableListOf<Card>()
    val dealerHand = mutableListOf<Card>()

    // Deal initial cards
    playerHand.add(deck.removeAt(0))
    dealerHand.add(deck.removeAt(0))
    playerHand.add(deck.removeAt(0))
    dealerHand.add(deck.removeAt(0))

    val newState = state.copy(
        deck = deck,
        playerHand = playerHand,
        dealerHand = dealerHand,
        currentBet = bet,
        gamePhase = GamePhase.PLAYING,
        dealerRevealed = false,
        resultMessage = "",
        playerBalance = state.playerBalance - bet,
        dealerMood = DealerMood.DEALING,
        showChipAnimation = true
    )

    // Check for immediate blackjack
    val playerValue = calculateHandValue(playerHand)
    val dealerValue = calculateHandValue(dealerHand)

    if (playerValue == 21) {
        return if (dealerValue == 21) {
            // Push
            newState.copy(
                gamePhase = GamePhase.GAME_OVER,
                dealerRevealed = true,
                resultMessage = "PUSH! Both Blackjack",
                playerBalance = newState.playerBalance + bet,
                dealerMood = DealerMood.THINKING
            )
        } else {
            // Player blackjack wins 3:2
            newState.copy(
                gamePhase = GamePhase.GAME_OVER,
                dealerRevealed = true,
                resultMessage = "BLACKJACK! You Win!",
                playerBalance = newState.playerBalance + (bet * 2.5).toInt(),
                dealerMood = DealerMood.SAD
            )
        }
    }

    return newState
}

private fun playerHit(state: BlackjackGameState): BlackjackGameState {
    val deck = state.deck.toMutableList()
    val playerHand = state.playerHand.toMutableList()

    playerHand.add(deck.removeAt(0))
    val handValue = calculateHandValue(playerHand)

    return if (handValue > 21) {
        // Player busts
        state.copy(
            deck = deck,
            playerHand = playerHand,
            gamePhase = GamePhase.GAME_OVER,
            dealerRevealed = true,
            resultMessage = "BUST! You Lose",
            dealerMood = DealerMood.HAPPY
        )
    } else if (handValue == 21) {
        // Auto-stand on 21
        val newState = state.copy(
            deck = deck,
            playerHand = playerHand,
            dealerMood = DealerMood.THINKING
        )
        playDealerTurn(playerStand(newState))
    } else {
        state.copy(
            deck = deck,
            playerHand = playerHand
        )
    }
}

private fun playerStand(state: BlackjackGameState): BlackjackGameState {
    return state.copy(
        gamePhase = GamePhase.DEALER_TURN,
        dealerRevealed = true,
        dealerMood = DealerMood.THINKING
    )
}

private fun playerDoubleDown(state: BlackjackGameState): BlackjackGameState {
    val deck = state.deck.toMutableList()
    val playerHand = state.playerHand.toMutableList()

    // Double the bet
    val newBet = state.currentBet * 2
    val newBalance = state.playerBalance - state.currentBet

    // Draw one card
    playerHand.add(deck.removeAt(0))
    val handValue = calculateHandValue(playerHand)

    return if (handValue > 21) {
        // Player busts
        state.copy(
            deck = deck,
            playerHand = playerHand,
            currentBet = newBet,
            playerBalance = newBalance,
            gamePhase = GamePhase.GAME_OVER,
            dealerRevealed = true,
            resultMessage = "BUST! You Lose",
            dealerMood = DealerMood.HAPPY
        )
    } else {
        // Stand after doubling
        val newState = state.copy(
            deck = deck,
            playerHand = playerHand,
            currentBet = newBet,
            playerBalance = newBalance,
            dealerMood = DealerMood.THINKING
        )
        playDealerTurn(playerStand(newState))
    }
}

private fun playDealerTurn(state: BlackjackGameState): BlackjackGameState {
    var deck = state.deck.toMutableList()
    var dealerHand = state.dealerHand.toMutableList()

    // Dealer hits until 17 or higher
    while (calculateHandValue(dealerHand) < 17) {
        dealerHand.add(deck.removeAt(0))
    }

    val playerValue = calculateHandValue(state.playerHand)
    val dealerValue = calculateHandValue(dealerHand)

    val (resultMessage, payout, mood) = when {
        dealerValue > 21 -> Triple("Dealer Busts! You WIN!", state.currentBet * 2, DealerMood.SAD)
        dealerValue > playerValue -> Triple("Dealer Wins. You Lose", 0, DealerMood.HAPPY)
        dealerValue < playerValue -> Triple("You WIN!", state.currentBet * 2, DealerMood.SAD)
        else -> Triple("PUSH! It's a Tie", state.currentBet, DealerMood.THINKING)
    }

    return state.copy(
        deck = deck,
        dealerHand = dealerHand,
        gamePhase = GamePhase.GAME_OVER,
        dealerRevealed = true,
        resultMessage = resultMessage,
        playerBalance = state.playerBalance + payout,
        dealerMood = mood,
        showChipAnimation = payout > 0
    )
}

private fun calculateHandValue(hand: List<Card>): Int {
    var value = 0
    var aces = 0

    for (card in hand) {
        if (card.rank == Rank.ACE) {
            aces++
            value += 11
        } else {
            value += card.rank.value
        }
    }

    // Adjust for aces if needed
    while (value > 21 && aces > 0) {
        value -= 10
        aces--
    }

    return value
}
