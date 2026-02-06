# DTO to Domain Mapping

Patterns for mapping DTOs and entities to domain models in the data layer.

## Table of Contents

1. [Basic Mapping Pattern](#basic-mapping-pattern)
2. [Nested Structure Mapping](#nested-structure-mapping)
3. [Collection Mapping](#collection-mapping)
4. [String Transformations](#string-transformations)
5. [Type Conversions](#type-conversions)
6. [Immutable Collections](#immutable-collections)

## Basic Mapping Pattern

### Extension Functions

Use extension functions on DTOs/entities for clean mapping:

```kotlin
// :features:jobs:data/mappers/JobMappers.kt
package com.example.features.jobs.data.mappers

import com.example.features.jobs.api.domain.Job
import com.example.features.jobs.data.dto.JobDto
import com.example.features.jobs.data.entity.JobEntity

// DTO -> Domain
internal fun JobDto.asDomain(): Job = Job(
    id = id,
    title = title,
    description = description,
    company = company,
    salary = salary?.let { Money(it, currency) }
)

// Entity -> Domain
internal fun JobEntity.asDomain(): Job = Job(
    id = id,
    title = title,
    description = description,
    company = company,
    salary = if (salaryAmount != null && salaryCurrency != null) {
        Money(salaryAmount, salaryCurrency)
    } else null
)

// Domain -> Entity (for saving to DB)
internal fun Job.toEntity(): JobEntity = JobEntity(
    id = id,
    title = title,
    description = description,
    company = company,
    salaryAmount = salary?.amount,
    salaryCurrency = salary?.currency
)
```

### Naming Conventions

| Direction | Function Name | Example |
|-----------|--------------|---------|
| DTO/Entity → Domain | `asDomain()` | `dto.asDomain()` |
| Domain → Entity | `toEntity()` | `domain.toEntity()` |
| Domain → DTO | `toDto()` | `domain.toDto()` |

## Nested Structure Mapping

### Complex DTO Example

```kotlin
// DTOs from API
@Serializable
internal data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TypeSlotDto>,
    val stats: List<StatDto>,
    val abilities: List<AbilitySlotDto>,
    val sprites: SpritesDto
)

@Serializable
internal data class TypeSlotDto(
    val slot: Int,
    val type: NamedResourceDto
)

@Serializable
internal data class StatDto(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedResourceDto
)

@Serializable
internal data class AbilitySlotDto(
    @SerialName("is_hidden") val isHidden: Boolean,
    val ability: NamedResourceDto
)

@Serializable
internal data class SpritesDto(
    @SerialName("front_default") val frontDefault: String?
)

@Serializable
internal data class NamedResourceDto(
    val name: String,
    val url: String
)
```

### Nested Mapping Implementation

```kotlin
internal fun PokemonDetailDto.asDomain(): PokemonDetail = PokemonDetail(
    id = id,
    name = name.replaceFirstChar { it.uppercase() },
    height = height,
    weight = weight,
    types = types.sortedBy { it.slot }.map { it.asDomain() }.toImmutableList(),
    stats = stats.map { it.asDomain() }.toImmutableList(),
    abilities = abilities.map { it.asDomain() }.toImmutableList(),
    imageUrl = sprites.frontDefault ?: ""
)

internal fun TypeSlotDto.asDomain(): Type = Type(
    name = type.name.replaceFirstChar { it.uppercase() },
    url = type.url
)

internal fun StatDto.asDomain(): Stat = Stat(
    name = stat.name.replace("-", " ").replaceFirstChar { it.uppercase() },
    value = baseStat
)

internal fun AbilitySlotDto.asDomain(): Ability = Ability(
    name = ability.name.replaceFirstChar { it.uppercase() },
    isHidden = isHidden
)
```

### Domain Models

```kotlin
// Immutable domain models
data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: ImmutableList<Type>,
    val stats: ImmutableList<Stat>,
    val abilities: ImmutableList<Ability>,
    val imageUrl: String
)

data class Type(
    val name: String,
    val url: String
)

data class Stat(
    val name: String,
    val value: Int
)

data class Ability(
    val name: String,
    val isHidden: Boolean
)
```

## Collection Mapping

### List Mapping

```kotlin
// Simple list mapping
internal fun List<JobDto>.asDomain(): List<Job> = 
    map { it.asDomain() }

// With filtering
internal fun List<JobDto>.asDomain(): List<Job> = 
    filter { it.isActive }
        .map { it.asDomain() }

// With sorting
internal fun List<JobDto>.asDomain(): List<Job> = 
    sortedByDescending { it.postedDate }
        .map { it.asDomain() }
```

### Map Mapping

```kotlin
// Grouping and mapping
internal fun List<JobDto>.asDomainByCategory(): Map<String, List<Job>> =
    groupBy { it.category }
        .mapValues { (_, jobs) -> jobs.map { it.asDomain() } }
```

### Paged Data

```kotlin
// Page mapping
internal fun JobPageDto.asDomain(): JobPage = JobPage(
    jobs = jobs.map { it.asDomain() }.toImmutableList(),
    hasMore = next != null,
    total = count
)
```

## String Transformations

### Common Transformations

```kotlin
// Capitalize first letter
internal fun String.capitalizeFirst(): String =
    replaceFirstChar { it.uppercase() }

// Capitalize words
internal fun String.capitalizeWords(): String =
    split(" ")
        .joinToString(" ") { it.capitalizeFirst() }

// Replace separators and capitalize
internal fun String.humanize(): String =
    replace("-", " ")
        .replace("_", " ")
        .capitalizeWords()
```

### Usage in Mappers

```kotlin
internal fun PokemonDto.asDomain(): Pokemon = Pokemon(
    id = id,
    // "bulbasaur" -> "Bulbasaur"
    name = name.capitalizeFirst(),
    // "hp" -> "Hp"
    type = type.capitalizeFirst()
)

internal fun StatDto.asDomain(): Stat = Stat(
    // "special-attack" -> "Special Attack"
    name = stat.name.humanize(),
    value = baseStat
)
```

## Type Conversions

### Numeric Conversions

```kotlin
// API returns String, domain needs Int
internal fun String?.toIntOrZero(): Int =
    this?.toIntOrNull() ?: 0

// Height in decimeters to meters
internal fun Int.decimetersToMeters(): Float =
    this / 10.0f

// Weight in hectograms to kilograms
internal fun Int.hectogramsToKilograms(): Float =
    this / 10.0f
```

### Date Conversions

```kotlin
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ISO-8601 string to LocalDateTime
internal fun String.toLocalDateTime(): LocalDateTime =
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())

// Unix timestamp to LocalDateTime
internal fun Long.toLocalDateTime(): LocalDateTime =
    Instant.fromEpochSeconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
```

### URL Handling

```kotlin
// Building URLs
internal fun String.toAbsoluteUrl(baseUrl: String): String =
    if (startsWith("http")) this else "$baseUrl$this"

// Extracting ID from URL
internal fun String.extractIdFromUrl(): Int? =
    substringAfterLast("/")
        .toIntOrNull()
```

## Immutable Collections

### Using kotlinx.collections.immutable

```kotlin
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet

// DTO -> Domain with immutable collections
internal fun PokemonDetailDto.asDomain(): PokemonDetail = PokemonDetail(
    id = id,
    name = name.capitalizeFirst(),
    types = types.map { it.asDomain() }.toImmutableList(),
    stats = stats.map { it.asDomain() }.toImmutableList(),
    abilities = abilities.map { it.asDomain() }.toImmutableList(),
    typeMap = types.associate { 
        it.type.name to it.slot 
    }.toImmutableMap()
)
```

### Why Immutable?

- **Thread safety**: Safe to share across threads
- **Defensive copying**: Prevents accidental mutations
- **Equals/hashCode**: Reliable value comparison
- **Functional style**: Encourages transformation over mutation

## Mapping Best Practices

### DO: Keep Mapping Simple

```kotlin
// ✅ CORRECT - Simple transformation
internal fun JobDto.asDomain(): Job = Job(
    id = id,
    title = title,
    company = company
)
```

### DON'T: Put Business Logic in Mappers

```kotlin
// ❌ WRONG - Business logic in mapper
internal fun JobDto.asDomain(): Job = Job(
    id = id,
    title = if (isUrgent) "URGENT: $title" else title,  // Logic belongs elsewhere
    isExpired = postedDate.isBefore(LocalDate.now().minusDays(30))
)

// ✅ CORRECT - Pure mapping
internal fun JobDto.asDomain(): Job = Job(
    id = id,
    title = title,
    postedDate = postedDate
)

// Logic in ViewModel or UseCase
val displayTitle = if (job.isUrgent) "URGENT: ${job.title}" else job.title
```

### DO: Handle Nulls Explicitly

```kotlin
// ✅ CORRECT - Explicit null handling
internal fun JobDto.asDomain(): Job = Job(
    id = id,
    salary = salary?.let { Money(it, currency ?: "USD") }
)

// ✅ CORRECT - Default values
internal fun JobDto.asDomain(): Job = Job(
    id = id,
    imageUrl = imageUrl ?: DEFAULT_IMAGE_URL
)
```

### DO: Document Complex Mappings

```kotlin
/**
 * Maps Pokemon DTO to domain model.
 * Note: Height is in decimeters from API, converted to meters.
 * Note: Weight is in hectograms from API, converted to kilograms.
 */
internal fun PokemonDto.asDomain(): Pokemon = Pokemon(
    id = id,
    name = name.capitalizeFirst(),
    heightMeters = height.decimetersToMeters(),
    weightKg = weight.hectogramsToKilograms()
)
```
