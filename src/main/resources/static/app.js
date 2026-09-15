const form = document.querySelector('#book-form');
const titleInput = document.querySelector('#title');
const authorInput = document.querySelector('#author');
const message = document.querySelector('#form-message');
const shelf = document.querySelector('#bookshelf');
const count = document.querySelector('#book-count');
const refreshButton = document.querySelector('#refresh-button');

async function loadBooks() {
  shelf.innerHTML = '<p class="loading">Loading your bookshelf...</p>';

  try {
    const response = await fetch('/books');
    if (!response.ok) throw new Error('Could not load books.');
    const books = await response.json();
    count.textContent = books.length;
    shelf.innerHTML = books.length ? books.map(renderBook).join('') : '<p class="empty">Your shelf is waiting for its first story.</p>';
  } catch (error) {
    shelf.innerHTML = `<p class="empty error">${error.message}</p>`;
  }
}

function renderBook(book) {
  return `<article class="book"><h3 class="book-title">${escapeHtml(book.title)}</h3><p class="book-author">${escapeHtml(book.author)}</p></article>`;
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
  }[character]));
}

form.addEventListener('submit', async event => {
  event.preventDefault();
  message.textContent = 'Saving...';

  try {
    const response = await fetch('/books', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title: titleInput.value.trim(), author: authorInput.value.trim() })
    });
    if (!response.ok) throw new Error('Could not save this book.');
    form.reset();
    message.textContent = 'Book added to your shelf.';
    await loadBooks();
  } catch (error) {
    message.textContent = error.message;
  }
});

refreshButton.addEventListener('click', loadBooks);
loadBooks();
