# Material Kit 3 - Symfony Application

A modern, responsive web application built with Symfony 6 and Material Kit 3, featuring beautiful UI components and a comprehensive set of pages.

## 🚀 Features

- **Modern Design**: Built with Material Kit 3 and Bootstrap 5
- **Responsive Layout**: Works perfectly on all devices
- **Comprehensive Pages**: Home, About, Contact, Sign In/Up, Profile, and more
- **Clean Code**: Well-structured Symfony application
- **Fast Performance**: Optimized for speed and efficiency

## 📋 Pages Included

- **Home Page**: Landing page with features and statistics
- **Presentation**: Component showcase and features overview
- **Landing Page**: Marketing-focused page with testimonials
- **About Us**: Company information and team details
- **Contact Us**: Contact form with FAQ section
- **Sign In/Up**: Authentication pages
- **Profile Page**: User profile management
- **Author Page**: Creator information and portfolio

## 🛠️ Technology Stack

- **Backend**: Symfony 6.0
- **Frontend**: Bootstrap 5.3.0
- **UI Kit**: Material Kit 3
- **Icons**: Material Symbols Rounded
- **JavaScript**: Vanilla JS with CountUp.js for animations

## 📦 Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd pi_nayrouz
   ```

2. **Install dependencies**
   ```bash
   composer install
   ```

3. **Configure environment**
   ```bash
   cp .env .env.local
   # Edit .env.local with your database configuration
   ```

4. **Start the development server**
   ```bash
   symfony server:start
   ```

5. **Visit the application**
   Open your browser and go to `http://localhost:8000`

## 🎨 Customization

### Colors and Styling
The application uses Bootstrap 5's CSS variables for easy customization. You can modify colors, spacing, and typography by editing the CSS in `templates/base.html.twig`.

### Adding New Pages
1. Create a new template in `templates/page/`
2. Add a new route in `src/Controller/MaterialKitController.php`
3. Update the navigation in `templates/base.html.twig`

### Components
The application includes various UI components:
- Cards with shadows and hover effects
- Forms with proper validation styling
- Buttons with different styles and sizes
- Progress bars and badges
- Accordion components
- Navigation and footer

## 📱 Responsive Design

The application is fully responsive and includes:
- Mobile-first approach
- Responsive navigation
- Flexible grid system
- Optimized typography for all screen sizes

## 🔧 Development

### Project Structure
```
pi_nayrouz/
├── src/
│   └── Controller/
│       └── MaterialKitController.php
├── templates/
│   ├── base.html.twig
│   └── page/
│       ├── index.html.twig
│       ├── about-us.html.twig
│       ├── contact-us.html.twig
│       ├── sign-in.html.twig
│       ├── sign-up.html.twig
│       ├── presentation.html.twig
│       ├── landing-page.html.twig
│       ├── profile-page.html.twig
│       └── author.html.twig
├── public/
├── config/
└── composer.json
```

### Key Features
- **Twig Templates**: Clean, maintainable template structure
- **Bootstrap Integration**: Full Bootstrap 5 support
- **Material Design**: Follows Material Design principles
- **Accessibility**: Built with accessibility in mind
- **Performance**: Optimized loading and rendering

## 🎯 Usage

### Navigation
The application includes a responsive navigation bar with links to all major pages. The navigation automatically collapses on mobile devices.

### Forms
All forms include proper validation styling and are ready for backend integration. The contact form and authentication forms are fully functional from a UI perspective.

### Components
The presentation page showcases various UI components that can be reused throughout the application.

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

## 🔄 Updates

The application is regularly updated with:
- Security patches
- New features
- Performance improvements
- Bug fixes

---

**Built with ❤️ using Symfony 6 and Material Kit 3**
