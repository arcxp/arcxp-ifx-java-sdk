resource "aws_codeartifact_domain" "arc-platform-domain" {
  domain = "com-arcpublishing-platform"
}

resource "aws_codeartifact_repository" "arc-platform-domain" {
  repository = "arc-platform-repository"
  domain     = aws_codeartifact_domain.arc-platform-domain.domain
}