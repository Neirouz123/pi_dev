# 🛒 Sliding Cart System - Documentation

## Overview

This project now includes a beautiful, interactive sliding cart system similar to Messenger's chat interface. The cart can slide in from the right side with smooth animations and 3D rotation effects, and it's available on all pages that extend the base template.

## ✨ Features

- **Smooth Animations**: 3D rotation and sliding effects using CSS transforms
- **Persistent Storage**: Cart items are saved in localStorage and persist across page refreshes
- **Responsive Design**: Works perfectly on all device sizes
- **Interactive Elements**: Edit, remove, and checkout functionality
- **Global Access**: Available on all pages that extend the base template
- **Keyboard Shortcuts**: Press ESC to close the cart
- **Real-time Updates**: Cart badge shows item count, total updates automatically

## 🚀 How to Use

### 1. Basic Setup

The cart system is automatically included when you extend from `base.html.twig`:

```twig
{% extends 'base.html.twig' %}

{% block title %}Your Page Title{% endblock %}

{% block body %}
    <!-- Your page content here -->
{% endblock %}
```

### 2. Adding Items to Cart

#### From JavaScript (anywhere on the page):
```javascript
// Add a venue to cart
const cartItem = {
    id: 'unique_id',
    venueName: 'Grand Ballroom',
    prix: 1500,
    date: '2024-01-15',
    heureDebut: '18:00',
    heureFin: '23:00',
    guests: 100,
    notes: 'Wedding celebration'
};

// Add to cart
if (window.cart) {
    window.cart.addToCart(cartItem);
}
```

#### From HTML buttons:
```html
<button onclick="addToCartFromForm()">Add to Cart</button>
<button onclick="quickAddToCart('Venue Name', 1000)">Quick Add</button>
```

### 3. Cart Controls

#### Open/Close Cart:
```javascript
// Open cart
window.cart.openCart();

// Close cart
window.cart.closeCart();

// Toggle cart
window.cart.toggleCart();
```

#### Manage Cart Items:
```javascript
// Get all cart items
const items = window.cart.getCartItems();

// Remove specific item
window.cart.removeItem(cartId);

// Update item
window.cart.updateItem(cartId, { prix: 1200 });

// Clear entire cart
window.cart.clearCart();

// Get cart total
const total = window.cart.total;
```

## 🎨 Customization

### Changing Cart Position

The cart slides in from the right by default. You can modify the position in the CSS:

```css
.sliding-cart {
    /* Change from right to left */
    right: auto;
    left: -400px;
    border-radius: 0 20px 20px 0;
}

.sliding-cart.open {
    left: 0;
}
```

### Changing Cart Theme

Modify the CSS variables in `base.html.twig`:

```css
:root {
    --royal-blue: #1a237e;
    --luxury-gold: #ffd700;
    --cart-bg: rgba(255, 255, 255, 0.95);
    --cart-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}
```

### Customizing Cart Size

```css
.sliding-cart {
    width: 400px;  /* Change width */
    height: 700px; /* Change height */
}
```

## 📱 Responsive Behavior

- **Desktop**: Full cart width (380px)
- **Mobile**: Full screen width with adjusted borders
- **Cart Toggle**: Automatically resizes for mobile devices

## 🔧 Integration Examples

### 1. E-commerce Product Page

```javascript
function addProductToCart(productId, productName, price) {
    const cartItem = {
        id: productId,
        productName: productName,
        price: price,
        quantity: 1,
        addedAt: new Date().toISOString()
    };
    
    window.cart.addToCart(cartItem);
}
```

### 2. Booking System

```javascript
function addBookingToCart(venue, date, time, guests) {
    const cartItem = {
        id: `booking_${Date.now()}`,
        venueName: venue.name,
        prix: venue.price,
        date: date,
        heureDebut: time.start,
        heureFin: time.end,
        guests: guests
    };
    
    window.cart.addToCart(cartItem);
}
```

### 3. Service Selection

```javascript
function addServiceToCart(service) {
    const cartItem = {
        id: service.id,
        serviceName: service.name,
        prix: service.price,
        duration: service.duration,
        category: service.category
    };
    
    window.cart.addToCart(cartItem);
}
```

## 🎯 Available Routes

- **Home Page**: `/` - Main page with venue booking
- **Cart Demo**: `/cart-demo` - Interactive demo of cart features

## 🐛 Troubleshooting

### Cart Not Appearing
1. Ensure the page extends `base.html.twig`
2. Check browser console for JavaScript errors
3. Verify that Bootstrap and Material Icons are loaded

### Items Not Saving
1. Check if localStorage is enabled in the browser
2. Verify the cart object is properly initialized
3. Check browser console for errors

### Animation Issues
1. Ensure CSS transitions are supported
2. Check for conflicting CSS rules
3. Verify z-index values are correct

## 🔮 Future Enhancements

- [ ] Cart item categories and filtering
- [ ] Advanced search within cart
- [ ] Cart sharing functionality
- [ ] Integration with backend APIs
- [ ] Advanced analytics and tracking
- [ ] Multi-language support
- [ ] Dark/light theme toggle

## 📚 API Reference

### Cart Class Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `addToCart(item)` | Add item to cart | `item: Object` | `void` |
| `removeItem(cartId)` | Remove item from cart | `cartId: String` | `void` |
| `updateItem(cartId, updates)` | Update item properties | `cartId: String, updates: Object` | `void` |
| `getCartItems()` | Get all cart items | None | `Array` |
| `clearCart()` | Remove all items | None | `void` |
| `openCart()` | Open cart panel | None | `void` |
| `closeCart()` | Close cart panel | None | `void` |
| `toggleCart()` | Toggle cart open/closed | None | `void` |

### Cart Item Structure

```javascript
{
    id: "unique_identifier",
    venueName: "Venue Name",
    prix: 1500,
    date: "2024-01-15",
    heureDebut: "18:00",
    heureFin: "23:00",
    guests: 100,
    notes: "Additional notes",
    cartId: "auto_generated_unique_id"
}
```

## 🤝 Contributing

To contribute to the cart system:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This cart system is part of the main project and follows the same license terms.

---

**Happy Shopping! 🛒✨**
