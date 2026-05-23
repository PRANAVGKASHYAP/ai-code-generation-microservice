import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { api, getUserInfo } from "@/lib/api";
import { SubscriptionResponse } from "@/lib/types";
import { useToast } from "@/hooks/use-toast";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { CreditCard, Check, ArrowLeft, Loader2, Crown, Sparkles, Calendar, ShieldCheck } from "lucide-react";

export function Settings() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [subscription, setSubscription] = useState<SubscriptionResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    fetchSubscription();
  }, []);

  const fetchSubscription = async () => {
    try {
      const data = await api.getSubscription();
      setSubscription(data);
    } catch (error) {
      console.error("Failed to fetch subscription:", error);
      // We can gracefully fall back to treatment as a free tier if the endpoint is not active yet or returns 404
      setSubscription({
        plan: {
          id: null,
          name: "Free Plan",
        },
        status: "INACTIVE",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSubscribe = async (planId: number) => {
    setActionLoading(true);
    try {
      const response = await api.createCheckout(planId);
      if (response.url) {
        window.location.href = response.url;
      } else {
        throw new Error("No redirect URL returned");
      }
    } catch (error: unknown) {
      console.error("Checkout redirection failed:", error);
      const errMsg = error instanceof Error ? error.message : "Failed to initiate Stripe Checkout session. Please try again.";
      toast({
        title: "Checkout Error",
        description: errMsg,
        variant: "destructive",
      });
      setActionLoading(false);
    }
  };

  const handleManageSubscription = async () => {
    setActionLoading(true);
    try {
      // Hitting POST /account/payments/portal
      const response = await api.createPortalSession();
      if (response.url) {
        window.location.href = response.url;
      } else {
        throw new Error("No redirect URL returned");
      }
    } catch (error: unknown) {
      console.error("Portal redirection failed:", error);
      const errMsg = error instanceof Error ? error.message : "Failed to open subscription management portal. Please try again.";
      toast({
        title: "Portal Error",
        description: errMsg,
        variant: "destructive",
      });
      setActionLoading(false);
    }
  };

  const isPro = subscription?.status === "ACTIVE";

  return (
    <div className="min-h-screen bg-background text-foreground relative overflow-hidden">
      {/* Background gradients for premium feel */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-[600px] h-[600px] bg-primary/5 rounded-full blur-3xl" />
        <div className="absolute bottom-1/4 right-1/4 w-[600px] h-[600px] bg-indigo-500/5 rounded-full blur-3xl" />
      </div>

      {/* Header */}
      <header className="border-b border-border/40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 relative z-10">
        <div className="container flex h-14 max-w-screen-2xl items-center px-4 sm:px-8">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate("/projects")}
            className="mr-4 text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Dashboard
          </Button>
          <div className="flex items-center gap-2 font-bold text-lg">
            <CreditCard className="w-5 h-5 text-primary" />
            <span>Billing & Settings</span>
          </div>
        </div>
      </header>

      {/* Content Container */}
      <main className="container max-w-4xl py-10 px-4 sm:px-8 relative z-10">
        <div className="mb-10">
          <h1 className="text-3xl font-bold tracking-tight">Account Billing</h1>
          <p className="text-muted-foreground mt-1">
            Manage your plan, payment methods, and billing details
          </p>
        </div>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-20 gap-4">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
            <p className="text-sm text-muted-foreground">Retrieving subscription status...</p>
          </div>
        ) : (
          <div className="space-y-8">
            {/* Status Card */}
            <Card className="border-border/60 bg-muted/20 backdrop-blur-sm relative overflow-hidden">
              <div className="absolute top-0 right-0 p-6 opacity-10 pointer-events-none">
                {isPro ? (
                  <Crown className="w-24 h-24 text-primary" />
                ) : (
                  <Sparkles className="w-24 h-24 text-muted-foreground" />
                )}
              </div>

              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="text-xl flex items-center gap-2">
                      Current Subscription
                      {isPro && (
                        <span className="inline-flex items-center gap-1 text-[10px] bg-primary/10 text-primary border border-primary/20 font-bold uppercase tracking-wider px-2 py-0.5 rounded-full">
                          <Crown className="w-3 h-3" /> {subscription?.plan?.name || "Pro"}
                        </span>
                      )}
                    </CardTitle>
                    <CardDescription>
                      Logged in as {getUserInfo()?.username || "authenticated user"}
                    </CardDescription>
                  </div>
                  <div className="text-right">
                    <span className="text-sm font-medium text-muted-foreground">Plan Status:</span>
                    <span
                      className={`block font-semibold text-sm ${
                        isPro ? "text-green-500" : "text-amber-500"
                      }`}
                    >
                      {subscription?.status || "INACTIVE"}
                    </span>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center p-4 rounded-lg bg-background/50 border border-border/40 gap-4">
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-md bg-primary/10 text-primary">
                      {isPro ? <Crown className="w-5 h-5" /> : <Sparkles className="w-5 h-5" />}
                    </div>
                    <div>
                      <p className="font-semibold">{subscription?.plan?.name || "Free Plan"}</p>
                      {isPro && subscription?.currentPeriodEnd && (
                        <p className="text-xs text-muted-foreground flex items-center gap-1 mt-0.5">
                          <Calendar className="w-3 h-3 text-muted-foreground" />
                          Renews on {new Date(subscription.currentPeriodEnd).toLocaleDateString()}
                        </p>
                      )}
                      {!isPro && (
                        <p className="text-xs text-muted-foreground mt-0.5">
                          Limited projects and generations
                        </p>
                      )}
                    </div>
                  </div>

                  {isPro ? (
                    <Button
                      onClick={handleManageSubscription}
                      disabled={actionLoading}
                      className="w-full sm:w-auto bg-primary text-primary-foreground hover:bg-primary/95 transition-all shadow-md gap-2"
                    >
                      {actionLoading && <Loader2 className="w-4 h-4 animate-spin" />}
                      Manage Subscription
                    </Button>
                  ) : (
                    <Button
                      onClick={() => document.getElementById("plans-pricing-grid")?.scrollIntoView({ behavior: "smooth" })}
                      className="w-full sm:w-auto bg-gradient-to-r from-primary to-indigo-600 hover:from-primary/90 hover:to-indigo-600/90 text-white transition-all shadow-lg shadow-primary/20 gap-2"
                    >
                      Upgrade / View Plans
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>

             {/* Plans Breakdown */}
            {!isPro && (
              <div id="plans-pricing-grid" className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-4">
                {/* Free Card */}
                <Card className="border-border/40 bg-background/50 relative flex flex-col justify-between">
                  <div>
                    <CardHeader>
                      <CardTitle className="text-lg font-bold text-muted-foreground">Free Tier</CardTitle>
                      <CardDescription>Get started building apps</CardDescription>
                      <div className="mt-4 flex items-baseline">
                        <span className="text-3xl font-extrabold">$0</span>
                        <span className="text-muted-foreground ml-1">/ month</span>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="h-px bg-border/40" />
                      <ul className="space-y-2.5 text-sm">
                        <li className="flex items-center gap-2.5 text-muted-foreground">
                          <Check className="w-4 h-4 text-green-500 shrink-0" />
                          <span>Up to 3 Active Projects</span>
                        </li>
                        <li className="flex items-center gap-2.5 text-muted-foreground">
                          <Check className="w-4 h-4 text-green-500 shrink-0" />
                          <span>Basic AI Assist Code Generation</span>
                        </li>
                        <li className="flex items-center gap-2.5 text-muted-foreground">
                          <Check className="w-4 h-4 text-green-500 shrink-0" />
                          <span>Standard Deployment Sandbox</span>
                        </li>
                        <li className="flex items-center gap-2.5 text-muted-foreground/50">
                          <Check className="w-4 h-4 text-muted-foreground/30 shrink-0" />
                          <span className="line-through">Priority Build Queues</span>
                        </li>
                        <li className="flex items-center gap-2.5 text-muted-foreground/50">
                          <Check className="w-4 h-4 text-muted-foreground/30 shrink-0" />
                          <span className="line-through">Unlimited Collaboration</span>
                        </li>
                      </ul>
                    </CardContent>
                  </div>
                  <CardFooter className="pt-6">
                    <Button variant="outline" className="w-full" disabled>
                      Current Plan
                    </Button>
                  </CardFooter>
                </Card>

                {/* Pro Card */}
                <Card className="border-primary/40 bg-primary/5 hover:border-primary/80 transition-all duration-300 relative flex flex-col justify-between overflow-hidden shadow-xl shadow-primary/5">
                  <div className="absolute top-0 right-0 bg-primary text-primary-foreground text-[10px] font-bold uppercase tracking-wider px-3 py-1 rounded-bl-lg">
                    Recommended
                  </div>
                  <div>
                    <CardHeader>
                      <CardTitle className="text-lg font-bold text-primary flex items-center gap-1.5">
                        <Crown className="w-4 h-4 text-primary" /> Pro Plan
                      </CardTitle>
                      <CardDescription>For creators and advanced builders</CardDescription>
                      <div className="mt-4 flex items-baseline">
                        <span className="text-3xl font-extrabold text-primary">$15</span>
                        <span className="text-muted-foreground ml-1">/ month</span>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="h-px bg-primary/20" />
                      <ul className="space-y-2.5 text-sm">
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-primary shrink-0" />
                          <span>Unlimited AI-Powered Projects</span>
                        </li>
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-primary shrink-0" />
                          <span>Pro Level Smart AI Generations</span>
                        </li>
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-primary shrink-0" />
                          <span>Fast Priority Build & Deploy Sandbox</span>
                        </li>
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-primary shrink-0" />
                          <span>Premium Live Code Previews</span>
                        </li>
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-primary shrink-0" />
                          <span>Advanced Project Collaboration & Sharing</span>
                        </li>
                      </ul>
                    </CardContent>
                  </div>
                  <CardFooter className="pt-6">
                    <Button
                      onClick={() => handleSubscribe(1)}
                      disabled={actionLoading}
                      className="w-full bg-primary hover:bg-primary/90 text-primary-foreground font-semibold shadow-lg shadow-primary/20 gap-2"
                    >
                      {actionLoading && <Loader2 className="w-4 h-4 animate-spin" />}
                      Upgrade Now
                    </Button>
                  </CardFooter>
                </Card>

                {/* Business Card */}
                <Card className="border-indigo-500/40 bg-indigo-500/5 hover:border-indigo-500/80 transition-all duration-300 relative flex flex-col justify-between overflow-hidden shadow-xl shadow-indigo-500/5">
                  <div className="absolute top-0 right-0 bg-indigo-500 text-white text-[10px] font-bold uppercase tracking-wider px-3 py-1 rounded-bl-lg">
                    Team Elite
                  </div>
                  <div>
                    <CardHeader>
                      <CardTitle className="text-lg font-bold text-indigo-400 flex items-center gap-1.5">
                        <Crown className="w-4 h-4 text-indigo-400" /> Business Plan
                      </CardTitle>
                      <CardDescription>For startups, agencies and team scale</CardDescription>
                      <div className="mt-4 flex items-baseline">
                        <span className="text-3xl font-extrabold text-indigo-400">$49</span>
                        <span className="text-muted-foreground ml-1">/ month</span>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="h-px bg-indigo-500/20" />
                      <ul className="space-y-2.5 text-sm">
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                          <span>Everything in Pro Plan</span>
                        </li>
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                          <span>Unlimited Team Collaborators</span>
                        </li>
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                          <span>White-labeled Code Preview Domains</span>
                        </li>
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                          <span>Dedicated Sandboxes & Virtual Cores</span>
                        </li>
                        <li className="flex items-center gap-2.5">
                          <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                          <span>24/7 Priority SLA Technical Support</span>
                        </li>
                      </ul>
                    </CardContent>
                  </div>
                  <CardFooter className="pt-6">
                    <Button
                      onClick={() => handleSubscribe(2)}
                      disabled={actionLoading}
                      className="w-full bg-indigo-500 hover:bg-indigo-500/90 text-white font-semibold shadow-lg shadow-indigo-500/20 gap-2"
                    >
                      {actionLoading && <Loader2 className="w-4 h-4 animate-spin" />}
                      Upgrade Business
                    </Button>
                  </CardFooter>
                </Card>
              </div>
            )}

            {/* Pro Features Showcase if already Pro */}
            {isPro && (
              <Card className="border-border/40 bg-gradient-to-r from-primary/5 to-indigo-500/5">
                <CardHeader>
                  <CardTitle className="text-sm font-semibold flex items-center gap-1.5 text-primary">
                    <ShieldCheck className="w-4 h-4" /> Pro Features Unlocked
                  </CardTitle>
                </CardHeader>
                <CardContent className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2">
                  <div className="p-4 rounded-lg bg-background/40 border border-border/20 text-center">
                    <p className="text-xs text-muted-foreground uppercase font-bold tracking-wider">
                      Projects Limit
                    </p>
                    <p className="text-2xl font-bold mt-1 text-primary">Unlimited</p>
                  </div>
                  <div className="p-4 rounded-lg bg-background/40 border border-border/20 text-center">
                    <p className="text-xs text-muted-foreground uppercase font-bold tracking-wider">
                      Build Priority
                    </p>
                    <p className="text-2xl font-bold mt-1 text-primary">High</p>
                  </div>
                  <div className="p-4 rounded-lg bg-background/40 border border-border/20 text-center">
                    <p className="text-xs text-muted-foreground uppercase font-bold tracking-wider">
                      Collaboration
                    </p>
                    <p className="text-2xl font-bold mt-1 text-primary">Full</p>
                  </div>
                </CardContent>
              </Card>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
