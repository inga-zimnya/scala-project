package tokens

import org.http4s.dsl.impl.QueryParamDecoderMatcher

object TokenQueryParamMatcher extends QueryParamDecoderMatcher[String]("token")