import { Component, ReactNode, ErrorInfo } from 'react';
import { Button } from 'Frontend/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from 'Frontend/components/ui/card';
import { FiHome, FiRefreshCw, FiAlertTriangle } from 'react-icons/fi';

interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

/**
 * ErrorBoundary Component
 *
 * Catches JavaScript errors anywhere in the child component tree,
 * logs those errors, and displays a fallback UI instead of crashing.
 *
 * This is a class component because React error boundaries
 * must be class components.
 *
 * @component
 *
 * @example
 * <ErrorBoundary>
 *   <Component />
 * </ErrorBoundary>
 *
 * @author Philipp Borkovic
 */
export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error('ErrorBoundary hat einen Fehler abgefangen:', error);
    console.error('Fehlerdetails:', errorInfo);

    this.setState({
      error,
      errorInfo,
    });
  }

  handleReload = (): void => {
    window.location.reload();
  };

  handleGoHome = (): void => {
    window.location.href = '/';
  };

  handleReset = (): void => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  render(): ReactNode {
    if (this.state.hasError) {
      const isDevelopment = import.meta.env.DEV;

      return (
        <div className="min-h-screen flex items-center justify-center bg-white p-4">
          <Card className="w-full max-w-2xl bg-white border shadow-lg">
            <CardHeader className="text-center">
              <div className="mx-auto mb-4 text-6xl text-red-600">
                <FiAlertTriangle className="inline-block" />
              </div>
              <CardTitle className="text-2xl text-gray-900">Anwendungsfehler</CardTitle>
              <CardDescription className="text-gray-600">
                Ein unerwarteter Fehler ist aufgetreten. Wir entschuldigen uns für die Unannehmlichkeiten.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {isDevelopment && this.state.error && (
                <div className="p-4 bg-red-50 border border-red-200 rounded-md">
                  <p className="font-semibold text-sm mb-2 text-red-600">
                    Fehlerdetails (Entwicklungsmodus):
                  </p>
                  <pre className="text-xs overflow-auto max-h-48 text-gray-800">
                    {this.state.error.toString()}
                    {this.state.errorInfo?.componentStack}
                  </pre>
                </div>
              )}

              <div className="space-y-3">
                <Button
                  onClick={this.handleReload}
                  className="w-full bg-black text-white hover:bg-black/90"
                  size="lg"
                >
                  <FiRefreshCw className="mr-2 h-5 w-5" />
                  Seite neu laden
                </Button>
                <Button
                  onClick={this.handleGoHome}
                  variant="outline"
                  className="w-full"
                  size="lg"
                >
                  <FiHome className="mr-2 h-5 w-5" />
                  Zur Startseite
                </Button>
                {isDevelopment && (
                  <Button
                    onClick={this.handleReset}
                    variant="secondary"
                    className="w-full"
                    size="lg"
                  >
                    Wiederherstellung versuchen
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
