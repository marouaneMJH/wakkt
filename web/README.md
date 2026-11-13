# wakkt website

Static site for the wakkt open-source project: landing page, presentation, privacy policy, terms and open-source info. No build step, no external requests (no CDN, fonts, analytics or cookies).

| Page | File |
|---|---|
| Landing | `index.html` |
| Presentation (10 slides, ←/→, F for fullscreen, print to PDF) | `presentation.html` |
| Privacy policy (use this URL in the Play Console) | `privacy.html` |
| Terms of use | `terms.html` |
| Open source: license, build, contributing | `open-source.html` |

## Preview locally
```
python3 -m http.server -d web 8000   # then open http://localhost:8000
```

## Publish on GitHub Pages
1. Push the repository to GitHub as `marouaneMJH/wakkt` (if the URL differs, update `REPO` links in the pages).
2. Repository Settings → Pages → Source: **GitHub Actions**.
3. Merge to `main`: `.github/workflows/pages.yml` deploys `web/` automatically (or run it manually from the Actions tab).
4. Privacy policy URL for the Play Console: `https://marouanemjh.github.io/wakkt/privacy.html`.
