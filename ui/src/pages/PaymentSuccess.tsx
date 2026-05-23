import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { api } from "@/lib/api";
import { PaymentVerification } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { CheckCircle2, AlertTriangle, Loader2, Sparkles, FolderHeart, ArrowRight } from "lucide-react";

type VerifyState = "loading" | "success" | "error";

export function PaymentSuccess() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [state, setState] = useState<VerifyState>("loading");
  const [verification, setVerification] = useState<PaymentVerification | null>(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [countdown, setCountdown] = useState(5);

  useEffect(() => {
    const sessionId = searchParams.get("session_id");

    if (!sessionId) {
      setState("error");
      setErrorMessage("No payment session identifier found. Did you access this page directly?");
      return;
    }

    verify(sessionId);
  }, [searchParams]);

  const verify = async (sessionId: string) => {
    try {
      const data = await api.verifyPayment(sessionId);
      if (data.status === "paid") {
        setVerification(data);
        setState("success");
      } else {
        setState("error");
        setErrorMessage("Payment verification indicates the session is unpaid.");
      }
    } catch (error: unknown) {
      console.error("Verification failed:", error);
      setState("error");
      const errMsg = error instanceof Error ? error.message : "An unexpected error occurred during payment verification.";
      setErrorMessage(errMsg);
    }
  };

  // Countdown timer for automatic redirection during Success state
  useEffect(() => {
    if (state !== "success") return;

    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          navigate("/projects");
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [state, navigate]);

  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center relative overflow-hidden px-4">
      {/* Visual background enhancements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-primary/10 rounded-full blur-3xl" />
        <div className="absolute top-1/4 left-1/3 w-[300px] h-[300px] bg-emerald-500/5 rounded-full blur-2xl" />
      </div>

      {state === "loading" && (
        <Card className="w-full max-w-md border-border/40 bg-background/60 backdrop-blur-md shadow-2xl relative z-10 text-center py-10">
          <CardContent className="space-y-6 flex flex-col items-center justify-center">
            <div className="relative">
              <Loader2 className="w-16 h-16 animate-spin text-primary" />
              <div className="absolute inset-0 flex items-center justify-center">
                <Sparkles className="w-6 h-6 text-primary animate-pulse" />
              </div>
            </div>
            <div className="space-y-2">
              <CardTitle className="text-xl">Verifying Payment</CardTitle>
              <p className="text-sm text-muted-foreground max-w-xs mx-auto">
                Securing your connection and upgrading your account...
              </p>
            </div>
          </CardContent>
        </Card>
      )}

      {state === "success" && (
        <Card className="w-full max-w-md border-emerald-500/20 bg-background/70 backdrop-blur-md shadow-2xl shadow-emerald-500/5 relative z-10 overflow-hidden">
          {/* Decorative success banner accent */}
          <div className="h-1.5 w-full bg-gradient-to-r from-emerald-500 to-teal-400" />
          
          <CardHeader className="text-center pt-8">
            <div className="mx-auto w-16 h-16 bg-emerald-500/10 rounded-full flex items-center justify-center text-emerald-500 mb-4 animate-bounce">
              <CheckCircle2 className="w-10 h-10" />
            </div>
            <CardTitle className="text-2xl font-bold tracking-tight text-emerald-500 flex items-center justify-center gap-2">
              Upgrade Successful! <CrownIcon />
            </CardTitle>
            <p className="text-sm text-muted-foreground mt-1">
              Thank you for upgrading to Project Companion Pro!
            </p>
          </CardHeader>

          <CardContent className="space-y-4 px-6">
            <div className="p-4 rounded-lg bg-emerald-500/5 border border-emerald-500/10 space-y-2.5">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Premium Account:</span>
                <span className="font-semibold text-foreground">{verification?.customerEmail}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Transaction Status:</span>
                <span className="font-semibold text-emerald-500 flex items-center gap-1">
                  Verified Paid
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Tier Granted:</span>
                <span className="font-bold text-primary flex items-center gap-1">
                  Unlimited Pro Access
                </span>
              </div>
            </div>

            <p className="text-xs text-center text-muted-foreground mt-4 italic">
              Redirecting you to your projects dashboard in {countdown}s...
            </p>

            {/* Countdown visual progress bar */}
            <div className="w-full h-1 bg-muted rounded-full overflow-hidden">
              <div 
                className="h-full bg-emerald-500 transition-all duration-1000 rounded-full"
                style={{ width: `${(countdown / 5) * 100}%` }}
              />
            </div>
          </CardContent>

          <CardFooter className="p-6 bg-muted/20 border-t border-border/40 flex flex-col gap-2">
            <Button
              onClick={() => navigate("/projects")}
              className="w-full bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white font-semibold shadow-lg shadow-emerald-500/10 gap-2 group"
            >
              Go to My Projects
              <ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
            </Button>
          </CardFooter>
        </Card>
      )}

      {state === "error" && (
        <Card className="w-full max-w-md border-destructive/20 bg-background/70 backdrop-blur-md shadow-2xl relative z-10 overflow-hidden">
          <div className="h-1.5 w-full bg-destructive" />

          <CardHeader className="text-center pt-8">
            <div className="mx-auto w-16 h-16 bg-destructive/10 rounded-full flex items-center justify-center text-destructive mb-4">
              <AlertTriangle className="w-10 h-10 animate-pulse" />
            </div>
            <CardTitle className="text-2xl font-bold tracking-tight text-destructive">
              Verification Failed
            </CardTitle>
            <p className="text-sm text-muted-foreground mt-1">
              We couldn't verify your Stripe Checkout payment session.
            </p>
          </CardHeader>

          <CardContent className="px-6 text-center">
            <div className="p-4 rounded-lg bg-destructive/5 border border-destructive/10 text-sm text-muted-foreground text-left whitespace-pre-line leading-relaxed">
              {errorMessage || "Invalid or expired session identifier. If your card was charged, it will be automatically provisioned, or contact support."}
            </div>
          </CardContent>

          <CardFooter className="p-6 bg-muted/20 border-t border-border/40 flex flex-col sm:flex-row gap-3">
            <Button
              variant="outline"
              onClick={() => navigate("/settings")}
              className="w-full"
            >
              Try Upgrading Again
            </Button>
            <Button
              onClick={() => navigate("/projects")}
              className="w-full bg-primary hover:bg-primary/95"
            >
              Return to Dashboard
            </Button>
          </CardFooter>
        </Card>
      )}
    </div>
  );
}

// Small inline Helper icon for Pro design
function CrownIcon() {
  return (
    <svg 
      xmlns="http://www.w3.org/2000/svg" 
      viewBox="0 0 24 24" 
      fill="currentColor" 
      className="w-5 h-5 text-amber-500 inline-block animate-pulse"
    >
      <path d="M2 19h20v2H2v-2zM22 6l-5 4-5-8-5 8-5-4 3 11h14l3-11z" />
    </svg>
  );
}
