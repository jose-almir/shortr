document.addEventListener('DOMContentLoaded', () => {
    const longUrlInput = document.getElementById('longUrl');
    const shortenBtn = document.getElementById('shortenBtn');
    const resultSection = document.getElementById('resultSection');
    const shortUrlSpan = document.getElementById('shortUrl');
    const copyBtn = document.getElementById('copyBtn');
    const errorMsg = document.getElementById('errorMsg');

    const shortenUrl = async () => {
        const longUrl = longUrlInput.value.trim();
        
        if (!longUrl) {
            showError('Please enter a URL');
            return;
        }

        try {
            shortenBtn.disabled = true;
            shortenBtn.textContent = 'Processing...';
            errorMsg.textContent = '';

            const response = await fetch('/shorten', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
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
            shortenBtn.textContent = 'Shorten Now';
        }
    };

    const showResult = (url) => {
        shortUrlSpan.textContent = url;
        resultSection.classList.remove('hidden');
        resultSection.scrollIntoView({ behavior: 'smooth' });
    };

    const showError = (msg) => {
        errorMsg.textContent = msg;
        resultSection.classList.add('hidden');
    };

    const copyToClipboard = () => {
        const text = shortUrlSpan.textContent;
        navigator.clipboard.writeText(text).then(() => {
            const originalColor = copyBtn.style.color;
            copyBtn.style.color = '#38bdf8';
            setTimeout(() => {
                copyBtn.style.color = originalColor;
            }, 2000);
        });
    };

    shortenBtn.addEventListener('click', shortenUrl);
    copyBtn.addEventListener('click', copyToClipboard);

    longUrlInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') shortenUrl();
    });
});
