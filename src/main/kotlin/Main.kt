import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.cache.normalized.api.*
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.watch
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.apollographql.apollo3.network.http.LoggingInterceptor
import com.example.IssueOrPullRequestQuery
import com.example.UpdatePullRequestReviewersMutation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

fun main(args: Array<String>) {
  val token = File("/users/mbonnin/git/test-reviewers/token").readText().trim()

  val apolloClient = ApolloClient.Builder()
    .serverUrl("https://api.github.com/graphql")
    .addHttpInterceptor(object : HttpInterceptor {
      override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain): HttpResponse {
        return chain.proceed(request.newBuilder().addHeader("Authorization", "Bearer $token").build())
      }
    })
    //.addHttpInterceptor(LoggingInterceptor())
    .normalizedCache(MemoryCacheFactory(), object : CacheKeyGenerator {
      override fun cacheKeyForObject(obj: Map<String, Any?>, context: CacheKeyGeneratorContext): CacheKey? {
        return obj["id"]?.toString()?.let { CacheKey(it) }
      }
    })
    .build()

  runBlocking {
    println("starting...")
    launch {
      println("start watching")
      apolloClient.query(
        IssueOrPullRequestQuery(
          repositoryOwner = "martinbonnin",
          repositoryName = "test-reviewers",
          number = 1
        )
      ).watch()
        .collect {
          println("Got watch ${it.data}")
        }
    }

    delay(1000)

    println("mutate")
    val mutationResponse = apolloClient.mutation(
      UpdatePullRequestReviewersMutation(
        id = "PR_kwDOHBKbgs40mGUD", // PR 1
        userIds = Optional.Present(listOf("MDQ6VXNlcjU4MjI2ODg3")) // logingithubtest
      )
    ).execute()

    println("Mutation response: ${mutationResponse.dataAssertNoErrors}")
  }
}