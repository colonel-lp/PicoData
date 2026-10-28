# Ella Backups public information pages

Static homepage and privacy policy for the owner's personal-use Google Drive OAuth client named **Ella Backups**. These files document its purpose and data use; they do not implement a backup job, receive Google tokens, or contain monitoring history.

For Pi installation and Google authentication, follow [Rclone setup](RCLONE-SETUP.md).

## Publish with GitHub Pages

In `colonel-lp/PicoData`, open **Settings → Pages**:

1. Source: **Deploy from a branch**.
2. Branch: **main**.
3. Folder: **/docs**.
4. Save and wait for the Pages deployment to finish.

Only this folder is the Pages publishing source. No custom build, external assets or JavaScript are required; `.nojekyll` selects plain static hosting.

Expected addresses, after Pages has been enabled and deployed:

- Homepage: https://colonel-lp.github.io/PicoData/
- Privacy policy: https://colonel-lp.github.io/PicoData/privacy.html

If GitHub reports a different public URL (for example because of an existing account-level custom domain), use that actual address and update these instructions.

## Google Auth Platform

On **Branding**, use the live homepage and privacy-policy addresses. Add `colonel-lp.github.io` under **Authorised domains** for the default URLs above, then save and return to **Audience** to publish the personal-use OAuth application. Publishing status and Google's brand-verification process are separate; these files do not guarantee Google's acceptance.

Use only the `https://www.googleapis.com/auth/drive.file` scope for this configuration. In `rclone config`, choose `drive.file` and the owner's own desktop OAuth client. Create the backup folder through rclone so that it is accessible with this scope. If different permissions or data practices are deliberately chosen later, update the pages before using them.

Keep all account details, OAuth client secrets/tokens, private configuration, backup archives and measurements outside these public files. Contact is through the existing public project issue tracker; no personal email address is published.

References:
- [GitHub Pages publishing source](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site)
- [Rclone Google Drive setup](https://rclone.org/drive/#making-your-own-client-id)
- [Google Drive scopes](https://developers.google.com/workspace/drive/api/guides/api-specific-auth)
