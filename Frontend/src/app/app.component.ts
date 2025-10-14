import { Component, OnInit, OnDestroy, Renderer2 } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Frontend';
  private mediaQuery: MediaQueryList;
  private mediaQueryListener: ((event: MediaQueryListEvent) => void) | null = null;

  constructor(private renderer: Renderer2) {
    // Initialize media query for dark mode detection
    this.mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
  }

  ngOnInit(): void {
    // Set initial theme based on browser preference
    this.applyTheme(this.mediaQuery.matches);

    // Listen for changes in browser theme preference
    this.mediaQueryListener = (event: MediaQueryListEvent) => {
      this.applyTheme(event.matches);
    };

    // Add event listener for theme changes
    this.mediaQuery.addEventListener('change', this.mediaQueryListener);
  }

  ngOnDestroy(): void {
    // Clean up event listener
    if (this.mediaQueryListener) {
      this.mediaQuery.removeEventListener('change', this.mediaQueryListener);
    }
  }

  /**
   * Apply theme based on dark mode preference
   * @param isDark - Whether dark mode is preferred
   */
  private applyTheme(isDark: boolean): void {
    const body = document.body;
    
    if (isDark) {
      this.renderer.removeClass(body, 'theme-light');
      this.renderer.addClass(body, 'theme-dark');
      console.log('Applied dark theme based on browser preference');
    } else {
      this.renderer.removeClass(body, 'theme-dark');
      this.renderer.addClass(body, 'theme-light');
      console.log('Applied light theme based on browser preference');
    }
  }
}
