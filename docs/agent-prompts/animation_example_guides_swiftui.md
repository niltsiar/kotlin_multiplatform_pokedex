# 🎨 Delightful SwiftUI Animation & Motion Guide

Last Updated: November 26, 2025

> **Platform**: Native iOS (SwiftUI)  
> **Companion Guide**: See `animation_example_guides.md` for Compose Multiplatform equivalents  
> **Project Context**: This guide supports the native SwiftUI iOS app (`:iosApp`) which integrates with KMP ViewModels via SKIE

---

## Overview

This guide provides **30+ essential SwiftUI animation patterns** for creating delightful, engaging iOS experiences. All patterns use native SwiftUI APIs and integrate seamlessly with shared KMP business logic.

**Animation Principles**:
- Use `.animation()` modifier with spring physics for natural feel
- Leverage `withAnimation` for explicit animation blocks
- Apply `.transition()` for view insertion/removal
- Use `matchedGeometryEffect` for hero animations
- Respect `.prefersReducedMotion` for accessibility

---

## 🎬 Screen & Page Transitions

### 1. Circular Reveal / Scale Transition

**When to use**: Screen transitions, modal presentations, expanding cards

```swift
struct ContentView: View {
    @State private var isExpanded = false
    
    var body: some View {
        content
            .scaleEffect(isExpanded ? 1.0 : 0.3)
            .opacity(isExpanded ? 1.0 : 0.0)
            .animation(.spring(response: 0.4, dampingFraction: 0.7), value: isExpanded)
    }
}
```

**Compose equivalent**: `AnimatedContent` with `scaleIn(initialScale = 0.3f)`

### 2. Slide Transition

**When to use**: Navigation pushes, sheet presentations

```swift
struct DetailView: View {
    var body: some View {
        content
            .transition(.asymmetric(
                insertion: .move(edge: .trailing).combined(with: .opacity),
                removal: .move(edge: .leading).combined(with: .opacity)
            ))
    }
}

// Usage
withAnimation(.spring()) {
    showDetail = true
}
```

**Compose equivalent**: `slideInHorizontally` + `fadeIn`

### 3. Hero Animation (Matched Geometry Effect)

**When to use**: Card → Detail transitions, shared element animations

```swift
struct ListView: View {
    @Namespace private var animation
    @State private var selectedId: Int?
    
    var body: some View {
        ForEach(items) { item in
            PokemonCard(pokemon: item)
                .matchedGeometryEffect(id: item.id, in: animation)
                .onTapGesture {
                    withAnimation(.spring(response: 0.6, dampingFraction: 0.8)) {
                        selectedId = item.id
                    }
                }
        }
        .overlay {
            if let selectedId {
                DetailView(pokemonId: selectedId)
                    .matchedGeometryEffect(id: selectedId, in: animation)
            }
        }
    }
}
```

**Compose equivalent**: Navigation 3 with `NavDisplay.transitionSpec()`

### 4. Fade Transition

**When to use**: Content swaps, loading states

```swift
struct ContentSwapper: View {
    @State private var showContent = false
    
    var body: some View {
        Group {
            if showContent {
                ContentView()
                    .transition(.opacity)
            } else {
                LoadingView()
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showContent)
    }
}
```

### 5. Page Curl Transition

**When to use**: Onboarding flows, book-style navigation

```swift
TabView {
    ForEach(pages) { page in
        OnboardingPage(page: page)
    }
}
.tabViewStyle(.page(indexDisplayMode: .always))
.animation(.easeInOut, value: currentPage)
```

---

## 🔘 Button & Micro-Interactions

### 6. Micro-Bounce (Scale Effect)

**When to use**: Button press feedback, tap interactions

```swift
struct BounceButton: View {
    @State private var isPressed = false
    
    var body: some View {
        Button(action: {}) {
            Text("Tap Me")
                .padding()
                .background(Color.blue)
                .cornerRadius(10)
        }
        .scaleEffect(isPressed ? 0.9 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.6), value: isPressed)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in isPressed = true }
                .onEnded { _ in isPressed = false }
        )
    }
}
```

**Compose equivalent**: `Modifier.graphicsLayer { scaleX = ...; scaleY = ... }`

### 7. Haptic Feedback

**When to use**: Success actions, errors, important taps

```swift
struct HapticButton: View {
    let impact = UIImpactFeedbackGenerator(style: .medium)
    let notification = UINotificationFeedbackGenerator()
    
    var body: some View {
        Button("Save") {
            // Success haptic
            notification.notificationOccurred(.success)
        }
        
        Button("Delete") {
            // Error haptic
            notification.notificationOccurred(.error)
        }
        
        Button("Toggle") {
            // Impact haptic
            impact.impactOccurred()
        }
    }
}
```

### 8. Rotation Animation

**When to use**: Loading indicators, refresh buttons

```swift
struct RotatingIcon: View {
    @State private var isRotating = false
    
    var body: some View {
        Image(systemName: "arrow.clockwise")
            .rotationEffect(.degrees(isRotating ? 360 : 0))
            .animation(.linear(duration: 1.0).repeatForever(autoreverses: false), value: isRotating)
            .onAppear { isRotating = true }
    }
}
```

### 9. Pulse Animation

**When to use**: Notifications, attention grabbers

```swift
struct PulsingBadge: View {
    @State private var isPulsing = false
    
    var body: some View {
        Circle()
            .fill(Color.red)
            .frame(width: 20, height: 20)
            .scaleEffect(isPulsing ? 1.2 : 1.0)
            .opacity(isPulsing ? 0.5 : 1.0)
            .animation(.easeInOut(duration: 0.8).repeatForever(autoreverses: true), value: isPulsing)
            .onAppear { isPulsing = true }
    }
}
```

### 10. Shape Morphing

**When to use**: Toggle states, icon transformations

```swift
struct MorphingShape: View {
    @State private var isCircle = true
    
    var body: some View {
        RoundedRectangle(cornerRadius: isCircle ? 50 : 10)
            .fill(Color.blue)
            .frame(width: 100, height: 100)
            .onTapGesture {
                withAnimation(.spring(response: 0.5, dampingFraction: 0.6)) {
                    isCircle.toggle()
                }
            }
    }
}
```

---

## 📜 List & Scroll Animations

### 11. Staggered List Entrance

**When to use**: List initial appearance, content reveals

```swift
struct StaggeredList: View {
    @State private var appearedIndices: Set<Int> = []
    let items: [Pokemon]
    
    var body: some View {
        List(items.indices, id: \.self) { index in
            PokemonCard(pokemon: items[index])
                .opacity(appearedIndices.contains(index) ? 1 : 0)
                .offset(y: appearedIndices.contains(index) ? 0 : 50)
                .onAppear {
                    withAnimation(.spring(response: 0.6, dampingFraction: 0.8).delay(Double(index) * 0.1)) {
                        appearedIndices.insert(index)
                    }
                }
        }
    }
}
```

**Compose equivalent**: `LazyColumn` with `AnimatedVisibility` and staggered delays

### 12. Pull-to-Refresh

**When to use**: List data refresh (native iOS pattern)

```swift
struct RefreshableList: View {
    @State private var items: [Pokemon] = []
    
    var body: some View {
        List(items) { item in
            PokemonCard(pokemon: item)
        }
        .refreshable {
            await loadItems()
        }
    }
    
    func loadItems() async {
        // Load data
    }
}
```

**Native API**: SwiftUI provides `.refreshable` modifier

### 13. Parallax Scrolling

**When to use**: Headers, background images

```swift
struct ParallaxHeader: View {
    var body: some View {
        ScrollView {
            GeometryReader { geometry in
                let offset = geometry.frame(in: .global).minY
                
                Image("header")
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: UIScreen.main.bounds.width, height: 300 + max(0, offset))
                    .offset(y: -offset)
                    .clipped()
            }
            .frame(height: 300)
            
            ContentView()
        }
    }
}
```

### 14. Swipe Actions

**When to use**: Delete, archive, quick actions

```swift
struct SwipeableRow: View {
    var body: some View {
        List {
            ForEach(items) { item in
                Text(item.name)
                    .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                        Button(role: .destructive) {
                            deleteItem(item)
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                    }
                    .swipeActions(edge: .leading) {
                        Button {
                            favoriteItem(item)
                        } label: {
                            Label("Favorite", systemImage: "star")
                        }
                        .tint(.yellow)
                    }
            }
        }
    }
}
```

### 15. Infinite Scroll Indicator

**When to use**: Pagination, loading more items

```swift
struct InfiniteScrollList: View {
    @State private var items: [Pokemon] = []
    @State private var isLoadingMore = false
    
    var body: some View {
        List {
            ForEach(items) { item in
                PokemonCard(pokemon: item)
                    .onAppear {
                        if item == items.last {
                            loadMore()
                        }
                    }
            }
            
            if isLoadingMore {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
            }
        }
    }
}
```

---

## 👆 Gesture-Based Interactions

### 16. Drag Gesture

**When to use**: Dismissible cards, reorderable lists

```swift
struct DraggableCard: View {
    @State private var offset = CGSize.zero
    @State private var isDismissed = false
    
    var body: some View {
        if !isDismissed {
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.blue)
                .frame(width: 300, height: 400)
                .offset(offset)
                .rotationEffect(.degrees(Double(offset.width / 20)))
                .gesture(
                    DragGesture()
                        .onChanged { gesture in
                            offset = gesture.translation
                        }
                        .onEnded { gesture in
                            if abs(gesture.translation.width) > 100 {
                                withAnimation(.spring()) {
                                    offset = CGSize(width: gesture.translation.width * 3, height: 0)
                                    isDismissed = true
                                }
                            } else {
                                withAnimation(.spring()) {
                                    offset = .zero
                                }
                            }
                        }
                )
        }
    }
}
```

**Compose equivalent**: `Modifier.pointerInput` with `detectDragGestures`

### 17. Long Press Gesture

**When to use**: Context menus, easter eggs

```swift
struct LongPressView: View {
    @State private var isPressed = false
    
    var body: some View {
        Circle()
            .fill(isPressed ? Color.green : Color.blue)
            .frame(width: 100, height: 100)
            .scaleEffect(isPressed ? 1.2 : 1.0)
            .onLongPressGesture(minimumDuration: 1.0) {
                withAnimation(.spring()) {
                    isPressed = true
                }
                // Trigger easter egg
                UINotificationFeedbackGenerator().notificationOccurred(.success)
            }
    }
}
```

### 18. Double Tap Gesture

**When to use**: Like actions, zoom toggles

```swift
struct DoubleTapView: View {
    @State private var isLiked = false
    @State private var scale: CGFloat = 1.0
    
    var body: some View {
        Image(systemName: isLiked ? "heart.fill" : "heart")
            .font(.system(size: 50))
            .foregroundColor(isLiked ? .red : .gray)
            .scaleEffect(scale)
            .onTapGesture(count: 2) {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                    isLiked.toggle()
                    scale = 1.3
                }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                    withAnimation(.spring()) {
                        scale = 1.0
                    }
                }
            }
    }
}
```

### 19. Magnification Gesture (Pinch to Zoom)

**When to use**: Image viewers, map zooming

```swift
struct ZoomableImage: View {
    @State private var scale: CGFloat = 1.0
    @State private var lastScale: CGFloat = 1.0
    
    var body: some View {
        Image("pokemon")
            .resizable()
            .aspectRatio(contentMode: .fit)
            .scaleEffect(scale)
            .gesture(
                MagnificationGesture()
                    .onChanged { value in
                        scale = lastScale * value
                    }
                    .onEnded { _ in
                        lastScale = scale
                        // Reset to bounds
                        withAnimation(.spring()) {
                            if scale < 1.0 {
                                scale = 1.0
                                lastScale = 1.0
                            } else if scale > 4.0 {
                                scale = 4.0
                                lastScale = 4.0
                            }
                        }
                    }
            )
    }
}
```

### 20. Rotation Gesture

**When to use**: Interactive elements, games

```swift
struct RotatableView: View {
    @State private var rotation: Angle = .zero
    
    var body: some View {
        RoundedRectangle(cornerRadius: 20)
            .fill(Color.blue)
            .frame(width: 200, height: 200)
            .rotationEffect(rotation)
            .gesture(
                RotationGesture()
                    .onChanged { angle in
                        rotation = angle
                    }
            )
    }
}
```

---

## 🎮 Device Interactions (iOS-Specific)

### 21. Shake Detection

**When to use**: Undo, easter eggs, hidden features

```swift
// In your View
struct ShakeDetectingView: View {
    @StateObject private var shakeDetector = ShakeDetector()
    
    var body: some View {
        content
            .onChange(of: shakeDetector.didShake) { didShake in
                if didShake {
                    withAnimation(.spring()) {
                        // Trigger easter egg
                    }
                    UINotificationFeedbackGenerator().notificationOccurred(.success)
                }
            }
    }
}

// ShakeDetector class
class ShakeDetector: ObservableObject {
    @Published var didShake = false
    
    init() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(deviceDidShake),
            name: UIDevice.deviceDidShakeNotification,
            object: nil
        )
    }
    
    @objc func deviceDidShake() {
        didShake.toggle()
    }
}

// Extension to detect shake
extension UIDevice {
    static let deviceDidShakeNotification = Notification.Name(rawValue: "deviceDidShakeNotification")
}

extension UIWindow {
    open override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        if motion == .motionShake {
            NotificationCenter.default.post(name: UIDevice.deviceDidShakeNotification, object: nil)
        }
    }
}
```

**Android equivalent**: SensorManager with accelerometer

### 22. Tilt Detection (CoreMotion)

**When to use**: Parallax effects, interactive backgrounds

```swift
import CoreMotion

class TiltDetector: ObservableObject {
    private let motionManager = CMMotionManager()
    @Published var tilt: CGFloat = 0
    
    func startMonitoring() {
        guard motionManager.isAccelerometerAvailable else { return }
        
        motionManager.accelerometerUpdateInterval = 0.1
        motionManager.startAccelerometerUpdates(to: .main) { [weak self] data, error in
            guard let data = data else { return }
            self?.tilt = CGFloat(data.acceleration.x)
        }
    }
    
    func stopMonitoring() {
        motionManager.stopAccelerometerUpdates()
    }
}

struct TiltResponsiveView: View {
    @StateObject private var tiltDetector = TiltDetector()
    
    var body: some View {
        Image("mascot")
            .offset(x: tiltDetector.tilt * 50)
            .onAppear { tiltDetector.startMonitoring() }
            .onDisappear { tiltDetector.stopMonitoring() }
    }
}
```

**Android equivalent**: SensorManager with accelerometer readings

### 23. Haptic Feedback (Advanced)

**When to use**: Rich tactile feedback for interactions

```swift
struct HapticFeedbackExamples: View {
    let impactLight = UIImpactFeedbackGenerator(style: .light)
    let impactMedium = UIImpactFeedbackGenerator(style: .medium)
    let impactHeavy = UIImpactFeedbackGenerator(style: .heavy)
    let selection = UISelectionFeedbackGenerator()
    let notification = UINotificationFeedbackGenerator()
    
    var body: some View {
        VStack(spacing: 20) {
            Button("Light Impact") {
                impactLight.impactOccurred()
            }
            
            Button("Medium Impact") {
                impactMedium.impactOccurred()
            }
            
            Button("Heavy Impact") {
                impactHeavy.impactOccurred()
            }
            
            Button("Selection Changed") {
                selection.selectionChanged()
            }
            
            Button("Success") {
                notification.notificationOccurred(.success)
            }
            
            Button("Warning") {
                notification.notificationOccurred(.warning)
            }
            
            Button("Error") {
                notification.notificationOccurred(.error)
            }
        }
    }
}
```

---

## ⏰ Time-Based & Contextual Animations

### 24. TimelineView for Dynamic Updates

**When to use**: Clocks, countdowns, dynamic content

```swift
struct AnimatedClock: View {
    var body: some View {
        TimelineView(.periodic(from: Date(), by: 1.0)) { context in
            let date = context.date
            let hour = Calendar.current.component(.hour, from: date)
            let minute = Calendar.current.component(.minute, from: date)
            
            // Easter egg: Special animation at 3:33
            if hour == 3 && minute == 33 {
                CosmicStarburstAnimation()
            } else {
                RegularClockView(date: date)
            }
        }
    }
}
```

### 25. Date/Time-Based Easter Eggs

**When to use**: Special occasions, time-sensitive features

```swift
struct TimeBasedEasterEgg: View {
    @State private var showEasterEgg = false
    
    var body: some View {
        content
            .onAppear {
                checkForEasterEgg()
            }
    }
    
    func checkForEasterEgg() {
        let calendar = Calendar.current
        let now = Date()
        let hour = calendar.component(.hour, from: now)
        let minute = calendar.component(.minute, from: now)
        
        // 12:34 - Rainbow confetti
        if hour == 12 && minute == 34 {
            withAnimation(.spring()) {
                showEasterEgg = true
            }
            // Trigger confetti animation
        }
        
        // User's birthday
        if calendar.isDateInToday(userBirthday) {
            // Show birthday mascot wave
        }
    }
}
```

---

## 🎨 Advanced Animation Patterns

### 26. Particle Effects with Canvas

**When to use**: Confetti, snow, star fields

```swift
struct ConfettiView: View {
    @State private var particles: [Particle] = []
    
    var body: some View {
        TimelineView(.animation) { context in
            Canvas { context, size in
                for particle in particles {
                    let rect = CGRect(x: particle.x, y: particle.y, width: 10, height: 10)
                    context.fill(Path(ellipseIn: rect), with: .color(particle.color))
                }
            }
        }
        .onAppear {
            startConfetti()
        }
    }
    
    func startConfetti() {
        particles = (0..<100).map { _ in
            Particle(
                x: CGFloat.random(in: 0...UIScreen.main.bounds.width),
                y: -20,
                color: [.red, .blue, .green, .yellow].randomElement()!,
                velocity: CGFloat.random(in: 2...5)
            )
        }
        
        Timer.scheduledTimer(withTimeInterval: 0.016, repeats: true) { _ in
            updateParticles()
        }
    }
    
    func updateParticles() {
        for i in particles.indices {
            particles[i].y += particles[i].velocity
            particles[i].x += CGFloat.random(in: -1...1)
        }
        particles.removeAll { $0.y > UIScreen.main.bounds.height }
    }
}

struct Particle {
    var x: CGFloat
    var y: CGFloat
    let color: Color
    let velocity: CGFloat
}
```

**Compose equivalent**: Canvas API with drawCircle

### 27. Animated Gradients

**When to use**: Backgrounds, loading states

```swift
struct AnimatedGradient: View {
    @State private var animateGradient = false
    
    var body: some View {
        LinearGradient(
            colors: [.blue, .purple, .pink],
            startPoint: animateGradient ? .topLeading : .bottomLeading,
            endPoint: animateGradient ? .bottomTrailing : .topTrailing
        )
        .animation(.easeInOut(duration: 3.0).repeatForever(autoreverses: true), value: animateGradient)
        .onAppear {
            animateGradient = true
        }
    }
}
```

### 28. Skeleton Loading

**When to use**: Content placeholders, loading states

```swift
struct SkeletonView: View {
    @State private var isAnimating = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            RoundedRectangle(cornerRadius: 10)
                .fill(Color.gray.opacity(0.3))
                .frame(height: 200)
            
            RoundedRectangle(cornerRadius: 5)
                .fill(Color.gray.opacity(0.3))
                .frame(height: 20)
            
            RoundedRectangle(cornerRadius: 5)
                .fill(Color.gray.opacity(0.3))
                .frame(width: 150, height: 20)
        }
        .overlay(
            GeometryReader { geometry in
                Rectangle()
                    .fill(
                        LinearGradient(
                            colors: [.clear, .white.opacity(0.4), .clear],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(width: geometry.size.width * 0.3)
                    .offset(x: isAnimating ? geometry.size.width : -geometry.size.width * 0.3)
            }
        )
        .clipped()
        .onAppear {
            withAnimation(.linear(duration: 1.5).repeatForever(autoreverses: false)) {
                isAnimating = true
            }
        }
    }
}
```

### 29. Spring Physics Playground

**When to use**: Understanding spring parameters

```swift
struct SpringPhysicsDemo: View {
    @State private var response: Double = 0.5
    @State private var dampingFraction: Double = 0.6
    @State private var isAnimating = false
    
    var body: some View {
        VStack {
            Circle()
                .fill(Color.blue)
                .frame(width: 50, height: 50)
                .offset(x: isAnimating ? 150 : -150)
                .animation(.spring(response: response, dampingFraction: dampingFraction), value: isAnimating)
            
            VStack {
                Text("Response: \(response, specifier: "%.2f")")
                Slider(value: $response, in: 0.1...2.0)
                
                Text("Damping: \(dampingFraction, specifier: "%.2f")")
                Slider(value: $dampingFraction, in: 0.1...1.0)
            }
            .padding()
            
            Button("Animate") {
                isAnimating.toggle()
            }
        }
    }
}
```

### 30. Custom Timing Curves

**When to use**: Precise control over animation feel

```swift
struct CustomTimingView: View {
    @State private var offset: CGFloat = 0
    
    var body: some View {
        Circle()
            .fill(Color.blue)
            .frame(width: 50, height: 50)
            .offset(x: offset)
            .onTapGesture {
                // Ease in-out cubic
                withAnimation(.timingCurve(0.42, 0, 0.58, 1, duration: 0.6)) {
                    offset = offset == 0 ? 150 : 0
                }
            }
    }
}
```

---

## 🎯 Accessibility & Reduced Motion

### 31. Respecting Reduced Motion

**When to use**: ALWAYS - accessibility requirement

```swift
struct AccessibleAnimation: View {
    @Environment(\.accessibilityReduceMotion) var reduceMotion
    @State private var isExpanded = false
    
    var body: some View {
        content
            .scaleEffect(isExpanded ? 1.2 : 1.0)
            .animation(reduceMotion ? .none : .spring(), value: isExpanded)
    }
}
```

---

## 🔗 Integration with KMP ViewModels

### Using SKIE with SwiftUI Animations

**Pattern**: Collect KMP `StateFlow` in `.task` and animate state changes

```swift
import Shared

struct PokemonListView: View {
    private var viewModel = KoinIosKt.getPokemonListViewModel()
    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    var body: some View {
        content
            .task {
                for await state in viewModel.uiState {
                    withAnimation(.spring()) {
                        self.uiState = state
                    }
                }
            }
    }
    
    @ViewBuilder
    private var content: some View {
        switch uiState {
        case is PokemonListUiStateLoading:
            ProgressView()
                .transition(.opacity)
        case let state as PokemonListUiStateContent:
            pokemonList(state.pokemons)
                .transition(.asymmetric(
                    insertion: .move(edge: .trailing).combined(with: .opacity),
                    removal: .opacity
                ))
        case let state as PokemonListUiStateError:
            errorView(state.message)
                .transition(.scale.combined(with: .opacity))
        default:
            EmptyView()
        }
    }
}
```

---

## 📚 Quick Reference: Compose → SwiftUI Mapping

| Compose API | SwiftUI Equivalent | Use Case |
|-------------|-------------------|----------|
| `AnimatedContent` | `.transition()` + conditional rendering | Screen transitions |
| `slideInHorizontally` | `.move(edge:)` | Slide animations |
| `fadeIn` | `.opacity` transition | Fade animations |
| `scaleIn` | `.scale` transition | Scale animations |
| `Modifier.graphicsLayer` | `.scaleEffect()`, `.rotationEffect()` | Transform animations |
| `AnimatedVisibility` | Conditional with `.transition()` | Show/hide animations |
| `LazyColumn` | `List` or `LazyVStack` | Scrolling lists |
| `Modifier.pointerInput` | `.gesture()` | Gesture handling |
| `spring()` | `.spring(response:dampingFraction:)` | Spring physics |
| `tween()` | `.easeInOut(duration:)` | Easing curves |

---

## 💡 Best Practices

1. **Use spring animations by default** - `.spring(response: 0.5, dampingFraction: 0.7)` feels natural
2. **Combine transitions** - `.combined(with:)` for richer effects
3. **Respect reduced motion** - Check `@Environment(\.accessibilityReduceMotion)`
4. **Add haptic feedback** - Enhance animations with tactile responses
5. **Test on device** - Simulator doesn't show true animation performance
6. **Use `.animation(_:value:)` over implicit animations** - More predictable behavior
7. **Leverage `withAnimation`** - Explicit animation blocks for state changes
8. **Keep animations short** - 0.3-0.6 seconds for most interactions
9. **Use `matchedGeometryEffect`** - For hero/shared element transitions
10. **Profile performance** - Use Instruments to check animation smoothness

---

## 🎬 Related Resources

- **Compose Animations**: See `animation_example_guides.md` for Compose Multiplatform equivalents
- **Device Interactions**: See `easter_eggs_and_mini_games_guide.md` for interactive patterns
- **iOS Integration**: See [`docs/tech/ios_integration.md`](../tech/ios_integration.md) for KMP ViewModel patterns
- **Apple HIG**: [Human Interface Guidelines - Motion](https://developer.apple.com/design/human-interface-guidelines/motion)

---

**Remember**: These animations should feel delightful, not distracting. Always prioritize user experience and accessibility.
