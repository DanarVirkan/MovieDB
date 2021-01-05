package com.moviedb.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage")
class MyIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(NamingDetector.ISSUE_NAMING)

    override val api: Int = CURRENT_API
}