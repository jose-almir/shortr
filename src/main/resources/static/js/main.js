/**
 * Shortr — main client-side logic.
 *
 * Handles URL shortening requests and clipboard copy with
 * visual feedback (icon swap + CSS class toggle).
 */
document.addEventListener('DOMContentLoaded', () => {
    const longUrlInput  = document.getElementById('longUrl');
    const shortenBtn    = document.getElementById('shortenBtn');
    const resultSection = document.getElementById('resultSection');
    const shortUrlSpan  = document.getElementById('shortUrl');
    const copyBtn       = document.getElementById('copyBtn');
    const errorMsg      = document.getElementById('errorMsg');

    const iconCopy  = copyBtn.querySelector('.icon-copy');
    const iconCheck = copyBtn.querySelector('.icon-check');

    /** Sends the URL to the backend and displays the result. */
    const shortenUrl = async () => {
        const longUrl = longUrlInput.value.trim();

        if (!longUrl) {
            showError('Please enter a URL');
            return;
        }

        try {
            shortenBtn.disabled = true;
            shortenBtn.querySelector('.btn-label').textContent = 'Processing…';
            errorMsg.textContent = '';

            const response = await fetch('/shorten', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ longUrl }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.longUrl || 'Invalid URL or server error');
            }

            const data = await response.json();
            const fullShortUrl = `${window.location.origin}/${data.shortCode}`;

            longUrlInput.value = '';
            showResult(fullShortUrl);
        } catch (error) {
            showError(error.message);
        } finally {
            shortenBtn.disabled = false;
            shortenBtn.querySelector('.btn-label').textContent = 'Shorten';
        }
    };

    /** Reveals the result section with the shortened URL. */
    const showResult = (url) => {
        shortUrlSpan.textContent = url;
        resultSection.classList.remove('hidden');

        // Re-trigger animation by forcing reflow
        resultSection.style.animation = 'none';
        resultSection.offsetHeight; // eslint-disable-line no-unused-expressions
        resultSection.style.animation = '';

        resultSection.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    };

    /** Displays an error message and hides any previous result. */
    const showError = (msg) => {
        errorMsg.textContent = msg;
        resultSection.classList.add('hidden');
    };

    /** Copies the short URL and swaps the icon briefly. */
    const copyToClipboard = () => {
        const text = shortUrlSpan.textContent;
        navigator.clipboard.writeText(text).then(() => {
            iconCopy.style.display  = 'none';
            iconCheck.style.display = 'block';
            copyBtn.classList.add('copied');

            setTimeout(() => {
                iconCopy.style.display  = 'block';
                iconCheck.style.display = 'none';
                copyBtn.classList.remove('copied');
            }, 1800);
        });
    };

    shortenBtn.addEventListener('click', shortenUrl);
    copyBtn.addEventListener('click', copyToClipboard);

    longUrlInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') shortenUrl();
    });
});
