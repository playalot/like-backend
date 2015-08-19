package utils

import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter
import play.filters.csrf.CSRFFilter
import play.filters.headers.SecurityHeadersFilter

/**
 * Provides filters.
 */
class Filters @Inject() (gzipFilter: GzipFilter, csrfFilter: CSRFFilter, securityHeadersFilter: SecurityHeadersFilter) extends HttpFilters {
  override def filters = Seq(csrfFilter, securityHeadersFilter, gzipFilter)
}
